package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GhostCard implements Card {

    @Override
    public String getName() {
        return "Ghost Card";
    }

    @Override
    public void leftClick(Player p) {
        p.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOW_FALLING, 200, 1));
    }

    @Override
    public void rightClick(Player p) {
        p.setAllowFlight(true);
        p.setFlying(true);

        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            p.setFlying(false);
            p.setAllowFlight(false);
        }, 20L * 40);
    }

    @Override
    public void shiftRightClick(Player p) {
        p.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY, 1200, 0));
    }
}
