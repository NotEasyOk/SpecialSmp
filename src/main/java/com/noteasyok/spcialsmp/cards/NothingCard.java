package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NothingCard implements Card {

    @Override
    public String getName() {
        return "Nothing Card";
    }

    @Override
    public void leftClick(Player p) {
        long time = p.getWorld().getTime();
        if (time > 12000) {
            p.getWorld().setTime(1000); // day
            p.sendMessage("Time set to day");
        } else {
            p.getWorld().setTime(14000); // night
            p.sendMessage("Time set to night");
        }
    }

    @Override
    public void rightClick(Player p) {
        // nothing
        p.sendMessage("Nothing Card: Right click does nothing.");
    }

    @Override
    public void shiftRightClick(Player p) {
        // Use Slow Falling instead of hacking damage events — 20s
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 20, 1, false, false, false));
        p.sendMessage("No fall damage for 20s (Slow Falling)");
    }
}
