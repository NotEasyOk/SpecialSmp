package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.Player;

public class NothingCard implements Card {

    public String getName() { return "Nothing"; }

    public void leftClick(Player p) {
        p.getWorld().setTime(p.getWorld().getTime() > 12000 ? 0 : 13000);
    }

    public void rightClick(Player p) {}

    public void shiftRightClick(Player p) {
        p.setFallDistance(0);
    }
}
