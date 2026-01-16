package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class WardenCard implements BaseCard {

    @Override
    public String getName() {
        return "Warden Card";
    }

    /* ---------------- LEFT CLICK ---------------- */
    @Override
    public void leftClick(Player p) {

        double baseMax = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();

        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
        p.setHealth(100.0);

        Bukkit.getScheduler().runTaskLater(
                SpcialSmp.get(),
                () -> {
                    if (!p.isOnline()) return;

                    p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseMax);
                    if (p.getHealth() > baseMax) {
                        p.setHealth(baseMax);
                    }
                },
                20L * 10
        );
    }

    /* ---------------- RIGHT CLICK (SONIC BOOM) ---------------- */
    @Override
    public void rightClick(Player p) {

        Location start = p.getEyeLocation();
        Vector dir = start.getDirection().normalize();

        for (int i = 1; i <= 20; i++) {

            Location point = start.clone().add(dir.clone().multiply(i));

            p.getWorld().spawnParticle(
                    Particle.SONIC_BOOM,
                    point,
                    1,
                    0, 0, 0,
                    0
            );

            for (Entity e : p.getWorld().getNearbyEntities(point, 1.5, 1.5, 1.5)) {
                if (e instanceof LivingEntity le && e != p) {

                    le.damage(12, p);
                    le.setVelocity(dir.clone().multiply(1.5));
                }
            }
        }

        p.getWorld().playSound(
                p.getLocation(),
                "entity.warden.sonic_boom",
                3f,
                1f
        );
    }

    /* ---------------- SHIFT + RIGHT ---------------- */
    @Override
    public void shiftRightClick(Player p) {

        p.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                20 * 15,
                3
        ));

        p.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                20 * 15,
                3
        ));
    }
}
