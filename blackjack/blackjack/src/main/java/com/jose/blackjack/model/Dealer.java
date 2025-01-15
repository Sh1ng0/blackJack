package com.jose.blackjack.model;


import java.util.List;

public class Dealer {

    private String name;
    private List<Card> hand;
    private int handValue;


    public Dealer() {
        this.name = "Dealer";

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }


    public void hit(Card card) {
        hand.add(card);

    }

    public void setHandValue(int handValue) {
        this.handValue = handValue;
    }


}