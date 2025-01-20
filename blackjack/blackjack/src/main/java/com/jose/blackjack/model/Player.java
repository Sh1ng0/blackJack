package com.jose.blackjack.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Table("player")
public class Player {

    @Id
    @Column("id")
    private Long playerId;

    private String name;

    private int score;

    @Transient
//    @JsonProperty("hand value: ")
    private List<Card> hand = new ArrayList<>();

    public Player() {
    }

    public Player(String playerName) {
        this.name = playerName;
        this.score = 0;
    }

    public void addScore(int points) {
        this.score += points;
    }


    public void hit(Card card) {
        hand.add(card);
        System.out.println(name + " recibe una carta: " + card);
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    @Override
    public String toString() {
        return "Player{" +
                "playerId=" + playerId +
                ", name='" + name + '\'' +
                ", score=" + score +
                ", hand=" + hand +
                '}';
    }

    public void setHandValue(int playerHandValue) {


    }
}