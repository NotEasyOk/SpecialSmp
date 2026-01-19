package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GhostCard extends BaseCard {

    // Cooldown track karne ke liye Map
    private final Map<String, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Ghost Card";
    }
    
    @Override
public int getModelData() {
    return 3;
}
    
    /* ---------------- LEFT CLICK (Slow Falling) ---------------- */
    @Override
    public void leftClick(Player p) {
        // Config path: cards.ghost.left_click_cooldown
        int cd = SpcialSmp.get().getConfig().getInt("cards.ghost.left_click_cooldown", 5);
        if (!isCool(p, "float", cd)) return;

        p.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOW_FALLING,
                20 * 10,
                0,
                false,
                false
        ));
    }

    /* ---------------- RIGHT CLICK (Fly 20s) ---------------- */
    @Override
    public void rightClick(Player p) {
        // Config path: cards.ghost.right_click_cooldown
        int cd = SpcialSmp.get().getConfig().getInt("cards.ghost.right_click_cooldown", 40);
        if (!isCool(p, "fly", cd)) return;

        p.setAllowFlight(true);
        p.setFlying(true);

        Bukkit.getScheduler().runTaskLater(
                SpcialSmp.get(),
                () -> {
                    if (!p.isOnline()) return;

                    p.setFlying(false);
                    p.setAllowFlight(false);
                    p.setFallDistance(0);
                },
                20L * 20 // 20 seconds
        );
    }

    /* ---------------- SHIFT + RIGHT CLICK (Spectator Phase) ---------------- */
    @Override
    public void shiftRightClick(Player p) {
        // Config path: cards.ghost.shift_click_cooldown
        int cd = SpcialSmp.get().getConfig().getInt("cards.ghost.shift_click_cooldown", 60);
        if (!isCool(p, "phase", cd)) return;

        GameMode old = p.getGameMode();
        p.setGameMode(GameMode.SPECTATOR);

        p.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                20 * 20,
                0,
                false,
                false
        ));

        Bukkit.getScheduler().runTaskLater(
                SpcialSmp.get(),
                () -> {
                    if (!p.isOnline()) return;

                    p.setGameMode(old);
                },
                20L * 20 // 20 seconds
        );
    }

    // --- COOLDOWN HELPER (Universal) ---
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
