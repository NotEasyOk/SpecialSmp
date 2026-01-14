package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GhostCard implements Card {

    @Override
    public String getName() {
        return "Ghost";
    }

    @Override
    public void leftClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
    }

    @Override
    public void rightClick(Player p) {
        p.setAllowFlight(true);
        p.setFlying(true);
    }

    @Override
    public void shiftRightClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1200, 0));
    }
}
