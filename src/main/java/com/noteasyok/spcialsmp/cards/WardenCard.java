package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WardenCard implements Card {

    @Override
    public String getName() {
        return "Warden";
    }

    @Override
    public void leftClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 1));
    }

    @Override
    public void rightClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 300, 4));
    }

    @Override
    public void shiftRightClick(Player p) {
        p.sendMessage("Magnet power activated (logic basic)");
    }
}
