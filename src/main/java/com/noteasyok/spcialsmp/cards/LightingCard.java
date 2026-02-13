package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LightingCard extends BaseCard implements Listener

    @Override
    public String getName() {
        return "Lighting Card";
    }
    
    @Override
public int getModelData() {
    return 5;
}

    @Override
public Material getMaterial() {
    return Material.YELLOW_DYE;
}
    
    /* ---------------- LEFT CLICK (Lightning + Strength) ---------------- */
    @Override
    public void leftClick(Player p) {
        // Config path: cards.lighting.left_click_cooldown
        int cd = SpcialSmp.get().getConfig().getInt("cards.lighting.left_click_cooldown", 15);
        if (!isCool(p, "strike", cd)) return;

        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                50
        );

        if (r == null || r.getHitPosition() == null) {
            // Target nahi mila to cooldown reset
            cooldowns.remove(p.getUniqueId().toString() + "_strike");
            return;
        }

        Location hit = r.getHitPosition().toLocation(p.getWorld());

        p.getWorld().strikeLightning(hit);
        p.getWorld().strikeLightning(hit);

        p.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                20 * 15,
                1
        ));
    }

    /* ---------------- RIGHT CLICK (Lightning Storm) ---------------- */
    @Override
    public void rightClick(Player p) {
        // Config path: cards.lighting.right_click_cooldown
        int cd = SpcialSmp.get().getConfig().getInt("cards.lighting.right_click_cooldown", 30);
        if (!isCool(p, "storm", cd)) return;

        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                60
        );

        if (r == null || r.getHitPosition() == null) {
            // Target nahi mila to cooldown reset
            cooldowns.remove(p.getUniqueId().toString() + "_storm");
            return;
        }

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
                5L
        );
    }

    /* ---------------- SHIFT + RIGHT ---------------- */
    @Override
    public void shiftRightClick(Player p) {
        // future power
    }

    // --- COOLDOWN HELPER ---
    private boolean isCool(Player p, String action) {
    // Purana 'seconds' wala logic hata do, manager config se khud seconds uthayega
    if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), action)) {
        long remaining = SpcialSmp.get().getCooldownManager().getRemainingSeconds(p, getName(), action);
        p.sendMessage("§cWait " + remaining + "s");
        return false;
    }

    // Cooldown apply manager ke through karo
    SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), action);
    return true;
    }
}
