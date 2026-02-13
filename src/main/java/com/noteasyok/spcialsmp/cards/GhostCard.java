package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GhostCard extends BaseCard {

    @Override
    public String getName() {
        return "Ghost Card";
    }
    
    @Override
public int getModelData() {
    return 3;
}

    @Override
public Material getMaterial() {
    return Material.WHITE_DYE;
}
    
    /* ---------------- LEFT CLICK (Slow Falling) ---------------- */
    @Override
    public void leftClick(Player p) {
        // Config path: cards.ghost.left_click_cooldown
        int cd = SpcialSmp.get().getConfig().getInt("cards.ghost.left_click_cooldown", 5);
        if (!isCool(p, "left", cd)) return;

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
        if (!isCool(p, "right", cd)) return;

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
                10L * 10 // 10 seconds
        );
    }

    /* ---------------- SHIFT + RIGHT CLICK (Spectator Phase) ---------------- */
    @Override
    public void shiftRightClick(Player p) {
        // Config path: cards.ghost.shift_click_cooldown
        int cd = SpcialSmp.get().getConfig().getInt("cards.ghost.shift_click_cooldown", 60);
        if (!isCool(p, "shift_right", cd)) return;

        GameMode old = p.getGameMode();
        p.setGameMode(GameMode.SPECTATOR);

        p.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                10 * 10,
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
                10L * 10 // 10 seconds
        );
    }

    // --- COOLDOWN HELPER (Universal) ---
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
