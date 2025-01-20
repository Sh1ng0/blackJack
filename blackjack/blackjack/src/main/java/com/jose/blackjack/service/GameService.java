package com.jose.blackjack.service;

import com.jose.blackjack.enums.GameState;
import com.jose.blackjack.model.*;
import com.jose.blackjack.repos.mongo.GameRepository;
import com.jose.blackjack.repos.sql.PlayerRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.SQLOutput;
import java.util.List;

import static reactor.core.publisher.Mono.just;

@Service
public class GameService {

    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final Game game;

    @Autowired
    public GameService(GameRepository gameRepository, PlayerRepository playerRepository, Game game) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.game = game;

    }

    public Mono<Game> createGame(String playerName) {


        Player player = new Player(playerName);
        Dealer dealer = new Dealer();
        Deck deck = new Deck();
        deck.initializeDeck();
        deck.shuffleDeck();


        this.game.setPlayer(player);
        this.game.setDealer(dealer);
        this.game.setDeck(deck);


        List<Card> playerHand = game.getDeck().dealCards(2);
        List<Card> dealerHand = game.getDeck().dealCards(2);
        game.getPlayer().setHand(playerHand);
        game.getDealer().setHand(dealerHand);


        int playerHandValue = calculateHandValue(playerHand);
    //        game.getPlayer().setHandValue(playerHandValue);
        int dealerHandValue = calculateHandValue(dealerHand);

        System.out.println("Valor de la mano: " + playerHandValue);
        System.out.println("Valor de la mano del dealer: "+ dealerHandValue);


        if (playerHandValue == 21) {
            System.out.println("¡Blackjack! El jugador gana automáticamente.");
            game.setState(GameState.PLAYER_WON);
            incrementPlayerScore(player);
        } else {
            System.out.println("La partida continúa.");
            game.setState(GameState.IN_PROGRESS);
        }
        // Guardar el Player y luego el Game
        return playerRepository.save(player)
                .flatMap(savedPlayer -> {
                    game.setPlayer(savedPlayer);
                    return gameRepository.save(game);
                });

    }


    public Mono<Game> hitOption() {
        System.out.println("El jugador decide tomar una carta (hit).");

        return playerRepository.findById(game.getPlayer().getPlayerId()) // Recuperar al Player desde la base de datos
                .flatMap(player -> {
                    // Ya que usamos @Transient debo reasignar la mano asi como el PLayah
                    player.setHand(game.getPlayer().getHand());
                    game.setPlayer(player);


                    Card newCard = game.getDeck().dealCards(1).get(0);
                    player.hit(newCard);
                    System.out.println(player.getName() + " ha tomado una carta: " + newCard);

                    int handValue = calculateHandValue(player.getHand());
                    System.out.println("Valor actual de la mano del jugador: " + handValue);

                    if (handValue > 21) {
                        System.out.println(player.getName() + " pierde, su valor excede 21.");
                        game.setState(GameState.HOUSE_WON);
                    } else if (handValue == 21) {
                        System.out.println("¡Blackjack para el jugador!");
                        game.setState(GameState.PLAYER_WON);
                        return incrementPlayerScore(player); // Incrementar puntuación si gana
                    } else {
                        System.out.println("La partida continúa.");
                        game.setState(GameState.IN_PROGRESS);
                    }

                    return playerRepository.save(player)
                            .flatMap(savedPlayer -> {
                                game.setPlayer(savedPlayer);
                                return gameRepository.save(game);
                            });
                });
    }


    public Mono<Game> standOption() {
        System.out.println("El jugador decide plantarse (stand).");

        return playerRepository.findById(game.getPlayer().getPlayerId())
                .flatMap(player -> {
                    // Recuperar el Player y asignar la mano actual desde el Game
                    player.setHand(game.getPlayer().getHand());
                    game.setPlayer(player);

                    System.out.println("Player actualizado con mano: " + player.getHand());
                    return dealerPlay(); // Pasar el turno al dealer
                });
    }


    public Mono<Game> doubleOption() {
        System.out.println("El jugador decide doblar la apuesta (double).");

        return playerRepository.findById(game.getPlayer().getPlayerId())
                .flatMap(player -> {

                    player.setHand(game.getPlayer().getHand());
                    game.setPlayer(player);

                    Card newCard = game.getDeck().dealCards(1).get(0);
                    player.getHand().add(newCard);

                    System.out.println(player.getName() + " ha tomado una carta al doblar: " + newCard);


                    int handValue = calculateHandValue(player.getHand());
                    System.out.println("Valor actual de la mano del jugador: " + handValue);


                    if (handValue > 21) {
                        System.out.println(player.getName() + " pierde, su valor excede 21.");
                        game.setState(GameState.HOUSE_WON);
                        return gameRepository.save(game);
                    }


                    return playerRepository.save(player)
                            .flatMap(savedPlayer -> {
                                game.setPlayer(savedPlayer);
                                return dealerPlay();
                            });
                });
    }


    public Mono<Game> dealerPlay() {
        System.out.println("Turno del dealer...");

        return playerRepository.findById(game.getPlayer().getPlayerId())
                .flatMap(player -> {
                    // Recuperar el Player y asignar la mano actual desde el Game
                    player.setHand(game.getPlayer().getHand());
                    game.setPlayer(player);

                    // Turno del dealer: sigue tomando cartas mientras el valor sea menor a 17
                    while (calculateHandValue(game.getDealer().getHand()) < 17) {
                        Card newCard = game.getDeck().dealCards(1).get(0);
                        game.getDealer().getHand().add(newCard);

                        System.out.println("El dealer ha tomado: " + newCard);
                        System.out.println("Valor actual de la mano del dealer: " + calculateHandValue(game.getDealer().getHand()));
                    }

                    // Calcular valores de las manos
                    int dealerHandValue = calculateHandValue(game.getDealer().getHand());
                    int playerHandValue = calculateHandValue(player.getHand());

                    // Evaluar los resultados
                    if (dealerHandValue > 21) {
                        System.out.println("El dealer pierde, su valor excede 21.");
                        game.setState(GameState.PLAYER_WON);
                        return incrementPlayerScore(player);
                    } else if (dealerHandValue == 21) {
                        System.out.println("Blackjack para el dealer. El jugador pierde.");
                        game.setState(GameState.HOUSE_WON);
                    } else if (dealerHandValue >= 17 && dealerHandValue <= 21) {
                        if (dealerHandValue > playerHandValue) {
                            System.out.println("El dealer gana con " + dealerHandValue + " frente a " + playerHandValue + ".");
                            game.setState(GameState.HOUSE_WON);
                        } else if (dealerHandValue < playerHandValue) {
                            System.out.println("El jugador gana con " + playerHandValue + " frente a " + dealerHandValue + ".");
                            game.setState(GameState.PLAYER_WON);
                            return incrementPlayerScore(player);
                        } else {
                            System.out.println("Empate: ambos tienen " + dealerHandValue + ".");
                            game.setState(GameState.STANDOFF);
                        }
                    }

                    return gameRepository.save(game);
                });
    }


    private Mono<Game> incrementPlayerScore(Player player) {
        int currentScore = player.getScore();
        player.setScore(currentScore + 1);
        System.out.println("El jugador ha ganado. Su nuevo score es: " + player.getScore());

        // Guardar el Player en SQL y luego el Game en Mongo
        return playerRepository.save(player)
                .flatMap(savedPlayer -> {
                    game.setPlayer(savedPlayer);
                    return gameRepository.save(game);
                });
    }


    public int calculateHandValue(List<Card> hand) {
        int totalValue = 0;
        int aceCount = 0;

        for (Card card : hand) {
            String value = card.getValue();
            if (value.equals("J") || value.equals("Q") || value.equals("K")) {
                totalValue += 10; // J, Q, K valen 10
            } else if (value.equals("A")) {
                aceCount++;
                totalValue += 11;
            } else {
                totalValue += Integer.parseInt(value);
            }
        }

        while (totalValue > 21 && aceCount > 0) {
            totalValue -= 10;
            aceCount--;
        }

        return totalValue;
    }

    public Mono<Game> getGameDetails(String gameId) {
        return gameRepository.findById(gameId);
    }


}


//    // UNDER CONSTRUCTION - doubleOption with the database hand retrieve logic //
//    public Mono<Game> doubleOption() {
//
//        System.out.println("El nota hace Double");
//
//        return playerRepository.findById(game.getPlayer().getPlayerId()) // Recuperar al Player desde la base de datos
//                .flatMap(player -> {
//                    // Ya que usamos @Transient debo reasignar la mano asi como el PLayah
//                    player.setHand(game.getPlayer().getHand());
//                    game.setPlayer(player);
//
//
//                    Card newCard = game.getDeck().dealCards(1).get(0);
//                    game.getPlayer().hit(newCard);
//
//                    int playerHandValue = game.getPlayer().getHandValue();
//                    System.out.println("Valor de la mano: " + playerHandValue);
//
//
//                    if (playerHandValue > 21) {
//                        System.out.println("El jugador se ha pasado de 21. ¡La casa gana!");
//
//                        game.setState(GameState.HOUSE_WON);
//                        return just(game);
//                    }
//
//
//                    if (playerHandValue == 21) {
//                        if (game.getState() == GameState.IN_PROGRESS)
//                            game.setState(GameState.PLAYER_WON);
//                        incrementPlayerScore(game.getPlayer());
//
//                        System.out.println("¡Has alcanzado 21! Veamos qué hace el dealer...");
//                        return just(game);
//                    }
//
//
//                    return playerRepository.save(player)
//                            .flatMap(savedPlayer -> {
//                                game.setPlayer(savedPlayer);
//                                return gameRepository.save(game);
//                            });
//                });
//
//    }
