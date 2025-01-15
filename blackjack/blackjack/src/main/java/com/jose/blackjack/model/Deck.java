package com.jose.blackjack.model;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private List<Card> cards;


    public Deck() {
        cards = new ArrayList<>();
    }


    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public void initializeDeck() {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

        for (String suit : suits) {
            for (String value : values) {
                cards.add(new Card(value, suit));
            }
        }
    }

    public void shuffleDeck() {
        Collections.shuffle(cards);

    }

    public List<Card> dealCards(int numCards) {
        if (numCards > cards.size()) {
            throw new IllegalArgumentException("Not enough cards in the deck");
        }

        List<Card> dealtCards = new ArrayList<>(cards.subList(0, numCards));


        cards.subList(0, numCards).clear();

        return dealtCards;
    }
}