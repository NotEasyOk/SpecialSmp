package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RuinCard implements Card {

    @Override
    public String getName() {
        return "Ruin Card";
    }

    @Override
    public void leftClick(Player p) {
        for (int i = 0; i < 10; i++) {
            p.getWorld().spawn(p.getLocation(), Silverfish.class);
        }
    }

    @Override
    public void rightClick(Player p) {
        // no power
    }

    @Override
    public void shiftRightClick(Player p) {
        p.getNearbyEntities(6, 6, 6).forEach(entity -> {
            if (entity instanceof Player target && !target.equals(p)) {
                target.addPotionEffect(
                        new PotionEffect(PotionEffectType.POISON, 200, 1)
                );
            }
        });
    }
}
