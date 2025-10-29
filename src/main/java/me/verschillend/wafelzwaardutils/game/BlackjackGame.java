package me.verschillend.wafelzwaardutils.game;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BlackjackGame {

    private final List<Card> deck;
    private final List<Card> playerHand;
    private final List<Card> dealerHand;
    private final double bet;
    private GameState state;

    public enum GameState {
        PLAYING, PLAYER_BUST, DEALER_BUST, PLAYER_BLACKJACK, DEALER_BLACKJACK, PLAYER_WIN, DEALER_WIN, PUSH
    }

    public BlackjackGame(double bet) {
        this.bet = bet;
        this.deck = new ArrayList<>();
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        this.state = GameState.PLAYING;
        initializeDeck();
        dealInitialCards();
    }

    private void initializeDeck() {
        String[] suits = {"♠", "♥", "♦", "♣"};
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(new Card(rank, suit));
            }
        }

        Collections.shuffle(deck);
    }

    private void dealInitialCards() {
        playerHand.add(drawCard());
        dealerHand.add(drawCard());
        playerHand.add(drawCard());
        dealerHand.add(drawCard());

        // Check for immediate blackjacks
        if (getHandValue(playerHand) == 21) {
            state = GameState.PLAYER_BLACKJACK;
        } else if (getHandValue(dealerHand) == 21) {
            state = GameState.DEALER_BLACKJACK;
        }
    }

    private Card drawCard() {
        if (deck.isEmpty()) {
            initializeDeck(); // Reshuffle if deck is empty
        }
        return deck.remove(0);
    }

    public void playerHit() {
        if (state != GameState.PLAYING) return;

        playerHand.add(drawCard());

        if (getHandValue(playerHand) > 21) {
            state = GameState.PLAYER_BUST;
        }
    }

    public void playerStand() {
        if (state != GameState.PLAYING) return;

        // Dealer draws until 17 or higher
        while (getHandValue(dealerHand) < 17) {
            dealerHand.add(drawCard());
        }

        int playerValue = getHandValue(playerHand);
        int dealerValue = getHandValue(dealerHand);

        if (dealerValue > 21) {
            state = GameState.DEALER_BUST;
        } else if (playerValue > dealerValue) {
            state = GameState.PLAYER_WIN;
        } else if (dealerValue > playerValue) {
            state = GameState.DEALER_WIN;
        } else {
            state = GameState.PUSH;
        }
    }

    public int getHandValue(List<Card> hand) {
        int value = 0;
        int aces = 0;

        for (Card card : hand) {
            value += card.getValue();
            if (card.getRank().equals("A")) {
                aces++;
            }
        }

        // Adjust for aces
        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }

        return value;
    }

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public List<Card> getDealerHand() {
        return dealerHand;
    }

    public double getBet() {
        return bet;
    }

    public GameState getState() {
        return state;
    }

    public double calculatePayout() {
        return switch (state) {
            case PLAYER_BLACKJACK -> bet * 2.5; // Blackjack pays 3:2
            case PLAYER_WIN, DEALER_BUST -> bet * 2.0; // Win pays 1:1
            case PUSH -> bet; // push returns bet
            default -> 0.0; // loss
        };
    }

    public static class Card {
        private final String rank;
        private final String suit;

        public Card(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }

        public String getRank() {
            return rank;
        }

        public String getSuit() {
            return suit;
        }

        public int getValue() {
            return switch (rank) {
                case "A" -> 11;
                case "J", "Q", "K" -> 10;
                default -> Integer.parseInt(rank);
            };
        }

        public Material getMaterial() {
            // map suits to materials for visual representation
            return switch (suit) {
                case "♠" -> Material.BLACK_STAINED_GLASS_PANE;
                case "♥" -> Material.RED_STAINED_GLASS_PANE;
                case "♦" -> Material.ORANGE_STAINED_GLASS_PANE;
                case "♣" -> Material.GRAY_STAINED_GLASS_PANE;
                default -> Material.WHITE_STAINED_GLASS_PANE;
            };
        }

        @Override
        public String toString() {
            return "§f" + rank + suit;
        }
    }
}