package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpicialSmp;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GhostCard implements Card {

    @Override
    public String getName() {
        return "Ghost Card";
    }

    /* ---------------- LEFT CLICK ---------------- */
    // Slow floating ghost movement
    @Override
    public void leftClick(Player p) {
        p.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOW_FALLING,
                20 * 10,
                0,
                false,
                false
        ));
    }

    /* ---------------- RIGHT CLICK ---------------- */
    // Fly only for 20 seconds
    @Override
    public void rightClick(Player p) {

        p.setAllowFlight(true);
        p.setFlying(true);

        Bukkit.getScheduler().runTaskLater(
                SpicialSmp.get(),
                () -> {
                    if (!p.isOnline()) return;

                    p.setFlying(false);
                    p.setAllowFlight(false);
                    p.setFallDistance(0);
                },
                20L * 20 // 20 seconds
        );
    }

    /* ---------------- SHIFT + RIGHT CLICK ---------------- */
    // Ghost phase mode (pass through blocks)
    @Override
    public void shiftRightClick(Player p) {

        GameMode old = p.getGameMode();
        p.setGameMode(GameMode.SPECTATOR);

        // invisibility safety (in case resource packs etc.)
        p.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                20 * 20,
                0,
                false,
                false
        ));

        Bukkit.getScheduler().runTaskLater(
                SpicialSmp.get(),
                () -> {
                    if (!p.isOnline()) return;

                    p.setGameMode(old);
                },
                20L * 20 // 20 seconds
        );
    }
}
