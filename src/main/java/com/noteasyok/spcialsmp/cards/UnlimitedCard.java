package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.Player;

public class UnlimitedCard implements Card {

    private final Card[] cards = {
            new EndermanCard(),
            new HerobrineCard(),
            new ZombieCard(),
            new WardenCard(),
            new CreeperCard(),
            new LightingCard(),
            new GhostCard(),
            new RuinCard(),
            new NothingCard()
    };

    @Override
    public String getName() {
        return "Unlimited";
    }

    @Override
    public void leftClick(Player p) {
        for (Card c : cards) c.leftClick(p);
    }

    @Override
    public void rightClick(Player p) {
        for (Card c : cards) c.rightClick(p);
    }

    @Override
    public void shiftRightClick(Player p) {
        for (Card c : cards) c.shiftRightClick(p);
    }
}
