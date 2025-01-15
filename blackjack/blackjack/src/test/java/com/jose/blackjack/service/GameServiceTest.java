package com.jose.blackjack.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.jose.blackjack.enums.GameState;
import com.jose.blackjack.model.*;
import com.jose.blackjack.repos.mongo.GameRepository;
import com.jose.blackjack.repos.sql.PlayerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.List;


@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private Game game; // Bean inyectado en GameService

    @InjectMocks
    private GameService gameService;


    @Test
    void createGame_shouldCreateAndPersistGameAndPlayer() {
        // Arrange
        String playerName = "TestPlayer";

        // Mock del jugador persistido
        Player savedPlayer = new Player(playerName);
        savedPlayer.setPlayerId(1L);

        // Cartas esperadas
        List<Card> playerHand = List.of(
                new Card("10", "Hearts"), // 10
                new Card("A", "Diamonds") // A
        );
        List<Card> dealerHand = List.of(
                new Card("K", "Spades"), // 10
                new Card("7", "Clubs")   // 7
        );

        // Mock del deck
        Deck deck = Mockito.spy(new Deck());
        Mockito.doNothing().when(deck).shuffleDeck(); // Evitar un shuffle aleatorio
        Mockito.doReturn(playerHand).doReturn(dealerHand).when(deck).dealCards(2); // Retornar manos específicas

        // Mock del game
        Mockito.when(game.getDeck()).thenReturn(deck);
        Mockito.when(game.getPlayer()).thenReturn(new Player(playerName));
        Mockito.when(game.getDealer()).thenReturn(new Dealer());

        // Mock repositorios
        Mockito.when(playerRepository.save(Mockito.any(Player.class))).thenReturn(Mono.just(savedPlayer));
        Mockito.when(gameRepository.save(Mockito.any(Game.class))).thenReturn(Mono.just(game));

        // Act
        Mono<Game> resultMono = gameService.createGame(playerName);

        // Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(gameResult -> {
                    // Verificar manos
                    Assertions.assertEquals(playerHand, gameResult.getPlayer().getHand());
                    Assertions.assertEquals(dealerHand, gameResult.getDealer().getHand());

                    // Verificar estado del juego
//                    int playerHandValue = calculateHandValue(playerHand);
//                    if (playerHandValue == 21) {
//                        Assertions.assertEquals(GameState.PLAYER_WON, gameResult.getState());  // Verificamos si el estado es PLAYER_WON
//                    } else {
//                        Assertions.assertEquals(GameState.IN_PROGRESS, gameResult.getState());
//                    }

                    // Verificar ID del Player
                    Assertions.assertNotNull(gameResult.getPlayer().getPlayerId());
                    return true;
                })
                .verifyComplete();

        // Verificar llamadas a los métodos
        Mockito.verify(playerRepository).save(Mockito.any(Player.class));
        Mockito.verify(gameRepository).save(Mockito.any(Game.class));
    }
    @Test
    void hitOption_shouldHandlePlayerHandCorrectly() {
        // Arrange
        Deck mockDeck = Mockito.mock(Deck.class);
        Player player = new Player("TestPlayer");
        Dealer dealer = new Dealer();
        Game game = new Game(player, dealer, mockDeck);

        GameService gameService = new GameService(gameRepository, playerRepository, game);

        // Caso 1: El jugador se pasa de 21
        List<Card> cardsOver21 = List.of(
                new Card("K", "Spades"), // 10
                new Card("7", "Hearts"), // 7
                new Card("5", "Clubs")   // 5 (sumará más de 21)
        );
        Mockito.when(mockDeck.dealCards(1)).thenReturn(List.of(cardsOver21.get(2)));
        player.setHand(cardsOver21.subList(0, 2));

        // Act & Assert: El jugador pierde
        StepVerifier.create(gameService.hitOption())
                .assertNext(updatedGame -> {
                    Assertions.assertEquals(GameState.HOUSE_WON, updatedGame.getState(), "El estado debería ser HOUSE_WON.");
                })
                .verifyComplete();

        // Caso 2: El jugador alcanza 21
        List<Card> cardsTo21 = List.of(
                new Card("K", "Diamonds"), // 10
                new Card("A", "Hearts")    // A (sumará 21)
        );
        Mockito.when(mockDeck.dealCards(1)).thenReturn(List.of(cardsTo21.get(1)));
        player.setHand(cardsTo21.subList(0, 1));

        // Act & Assert: El jugador gana
        StepVerifier.create(gameService.hitOption())
                .assertNext(updatedGame -> {
                    Assertions.assertEquals(GameState.PLAYER_WON, updatedGame.getState(), "El estado debería ser PLAYER_WON.");
                })
                .verifyComplete();

        // Caso 3: El jugador no alcanza 21
        List<Card> cardsInProgress = List.of(
                new Card("8", "Clubs"), // 8
                new Card("7", "Diamonds") // 7 (sumará menos de 21)
        );
        Mockito.when(mockDeck.dealCards(1)).thenReturn(List.of(new Card("2", "Hearts"))); // +2, total 17
        player.setHand(cardsInProgress);

        // Act & Assert: La partida sigue en progreso
        StepVerifier.create(gameService.hitOption())
                .assertNext(updatedGame -> {
                    Assertions.assertEquals(GameState.IN_PROGRESS, updatedGame.getState(), "El estado debería ser IN_PROGRESS.");
                })
                .verifyComplete();
    }






    private int calculateHandValue(List<Card> hand) {
        int totalValue = 0;
        int aceCount = 0;

        for (Card card : hand) {
            String value = card.getValue();
            if (value.equals("J") || value.equals("Q") || value.equals("K")) {
                totalValue += 10; // J, Q, K valen 10
            } else if (value.equals("A")) {
                aceCount++; // Contamos los ases para tratarlos más tarde
                totalValue += 11; // Inicialmente asumimos que el as vale 11
            } else {
                totalValue += Integer.parseInt(value); // Las cartas numéricas valen su valor
            }
        }


        while (totalValue > 21 && aceCount > 0) {
            totalValue -= 10;
            aceCount--;
        }

        return totalValue;
    }



}
