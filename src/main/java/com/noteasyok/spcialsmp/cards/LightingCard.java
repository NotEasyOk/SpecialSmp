package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

public class LightingCard implements BaseCard {

    @Override
    public String getName() {
        return "Lighting Card";
    }

    /* ---------------- LEFT CLICK ---------------- */
    @Override
    public void leftClick(Player p) {

        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                50
        );

        if (r == null || r.getHitPosition() == null) return;

        Location hit = r.getHitPosition().toLocation(p.getWorld());

        // 2 lightning strikes
        p.getWorld().strikeLightning(hit);
        p.getWorld().strikeLightning(hit);

        // Strength II for 15 seconds
        p.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                20 * 15,
                1
        ));
    }

    /* ---------------- RIGHT CLICK ---------------- */
    @Override
    public void rightClick(Player p) {

        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                60
        );

        if (r == null || r.getHitPosition() == null) return;

        Location center = r.getHitPosition().toLocation(p.getWorld());

        long end = System.currentTimeMillis() + 5000;

        Bukkit.getScheduler().runTaskTimer(
                SpcialSmp.get(),
                task -> {

                    if (!p.isOnline() || System.currentTimeMillis() > end) {
                        task.cancel();
                        return;
                    }

                    double x = center.getX() + (Math.random() * 6 - 3);
                    double z = center.getZ() + (Math.random() * 6 - 3);

                    Location strike = new Location(
                            center.getWorld(),
                            x,
                            center.getY(),
                            z
                    );

                    center.getWorld().strikeLightning(strike);

                },
                0L,
                5L // fast storm
        );
    }

    /* ---------------- SHIFT + RIGHT ---------------- */
    @Override
    public void shiftRightClick(Player p) {
        // future power
    }
}
