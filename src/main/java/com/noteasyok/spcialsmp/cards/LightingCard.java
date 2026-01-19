package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LightingCard extends BaseCard {

    // Cooldown track karne ke liye Map
    private final Map<String, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Lighting Card";
    }
    
    @Override
public int getModelData() {
    return 5;
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
    private boolean isCool(Player p, String key, int seconds) {
        if (seconds <= 0) return true;
        long now = System.currentTimeMillis();
        
        // Map ki key String honi chahiye
        String mapKey = p.getUniqueId().toString() + "_" + key;
        
        if (cooldowns.containsKey(mapKey)) {
            long timeLeft = (cooldowns.get(mapKey) - now) / 1000;
            if (timeLeft > 0) {
                // Config se message uthayega
                String rawMsg = SpcialSmp.get().getConfig().getString("messages.cooldown-active", "§cWait %time%s");
                p.sendMessage(rawMsg.replace("%time%", String.valueOf(timeLeft)));
                return false;
            }
        }
        cooldowns.put(mapKey, now + (seconds * 1000L));
        return true;
    }
        }
