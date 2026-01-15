package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WardenCard implements Card {

    @Override
    public String getName() {
        return "Warden Card";
    }

    @Override
    public void leftClick(Player p) {
        for (Entity e : p.getNearbyEntities(10,10,10)) {
            if (e instanceof Player pl && !pl.equals(p)) {
                pl.addPotionEffect(new PotionEffect(
                        PotionEffectType.DARKNESS, 200, 1));
            }
        }
    }

    @Override
    public void rightClick(Player p) {
        p.getWorld().createExplosion(
                p.getLocation().add(p.getLocation().getDirection().multiply(3)),
                4f, false, false);
    }

    @Override
    public void shiftRightClick(Player p) {
        double base = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);

        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(base);
            if (p.getHealth() > base) p.setHealth(base);
        }, 20L * 15);
    }
}
