package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.Player;

public class LightingCard implements Card {

    @Override
    public String getName() {
        return "Lighting";
    }

    @Override
    public void leftClick(Player p) {
        p.getWorld().strikeLightning(p.getLocation());
    }

    @Override
    public void rightClick(Player p) {
        boolean storm = p.getWorld().hasStorm();
        p.getWorld().setStorm(!storm);
    }

    @Override
    public void shiftRightClick(Player p) {
        p.sendMessage("Lightning trail enabled (no damage)");
    }
}
