package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RuinWorldListener implements Listener {

    public RuinWorldListener() {
        Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), () -> {
            World ruinWorld = Bukkit.getWorld("world_ruin_dimension");
            if (ruinWorld == null) return;

            for (Player p : ruinWorld.getPlayers()) {
                // Falling Ash Particles (1.21 style)
                Location loc = p.getLocation();
                // Spore and Ash effects
                p.spawnParticle(Particle.WARPED_SPORE, loc.add(0, 5, 0), 60, 8, 4, 8, 0.01);
                p.spawnParticle(Particle.WHITE_ASH, loc, 30, 8, 4, 8, 0.01);

                // Poison Water Check
                if (p.getLocation().getBlock().getType() == Material.WATER) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 1));
                }
            }
        }, 0L, 20L);
    }
}
