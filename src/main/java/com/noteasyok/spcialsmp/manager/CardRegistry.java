package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.cards.*;

import java.util.HashMap;
import java.util.Map;

public class CardRegistry {

    private static final Map<String, Card> CARDS = new HashMap<>();

    public static void registerAll() {
        register(new EndermanCard());
        register(new HerobrineCard());
        register(new NothingCard());
        register(new ZombieCard());
        register(new WardenCard());
        register(new CreeperCard());
        register(new LightingCard());
        register(new GhostCard());
        register(new RuinCard());
        register(new UnlimitedCard());
    }

    private static void register(Card card) {
        CARDS.put(card.getName() + " Card", card);
    }

    public static Map<String, Card> getCards() {
        return CARDS;
    }

    public static Card get(String displayName) {
        return CARDS.get(displayName);
    }
}

