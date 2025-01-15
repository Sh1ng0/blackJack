package com.jose.blackjack.model;




import com.jose.blackjack.enums.GameState;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "games")
public class Game {

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
    }



    @Id
    private String id;
    private Player player;
    private Dealer dealer;
    private Deck deck;
    private boolean finished;
    private GameState state;


    public Game(Player player, Dealer dealer, Deck deck) {
        this.player = player;
        this.dealer = dealer;
        this.deck = deck;

    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public Game() {
        // Solo asignar IN_PROGRESS si no hay un estado previo
        if (state == null) {
            state = GameState.IN_PROGRESS;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }



    @Override
    public String toString() {
        return "Game{" +
                "id='" + id + '\'' +
                ", player=" + player +
                ", dealer=" + dealer +
                ", deck=" + deck +
                ", finished=" + finished +
                '}';
    }
}