package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class WardenCard implements Card {

    @Override
    public String getName() { return "Warden Card"; }

    @Override
    public void leftClick(Player p) {
        // darkness to nearby enemies (radius 10)
        for (Entity e : p.getNearbyEntities(10,10,10)) {
            if (e instanceof Player other && !other.equals(p)) {
                other.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 1));
            }
        }
    }

    @Override
    public void rightClick(Player p) {
        // sonic boom: explosion in front, then health boost
        Location center = p.getLocation().add(p.getLocation().getDirection().multiply(3));
        p.getWorld().createExplosion(center, 4f, false, false);

        // temporary max health boost to player
        double original = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(60.0); // set to 60 HP (30 hearts)
        p.setHealth(Math.min(p.getHealth(), 60.0));

        // schedule revert after 15s
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            if (p.isOnline()) {
                p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(original);
                if (p.getHealth() > original) p.setHealth(original);
            }
        }, 20L * 15);
    }

    @Override
    public void shiftRightClick(Player p) {
        // magnet: pull nearby items and dropped blocks toward player for 10s
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            for (Entity e : p.getNearbyEntities(10, 10, 10)) {
                if (e instanceof org.bukkit.entity.Item || e instanceof org.bukkit.entity.FallingBlock) {
                    Vector dir = p.getLocation().toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.5);
                    e.setVelocity(dir);
                }
            }
        }, 0L);
    }
}
