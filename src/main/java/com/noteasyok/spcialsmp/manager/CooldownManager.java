package com.noteasyok.spcialsmp.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    // Map<UUID, Map<cardName, expireMillis>>
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public boolean canUse(Player player, String cardName) {
        UUID id = player.getUniqueId();
        if (!cooldowns.containsKey(id)) return true;
        Map<String, Long> map = cooldowns.get(id);
        if (!map.containsKey(cardName)) return true;
        long expire = map.get(cardName);
        return System.currentTimeMillis() > expire;
    }

    // apply default cooldown (from config) or given seconds
    public void applyCooldown(Player player, String cardName) {
        int sec = 60; // default
        try {
            sec = SpcialSmp.get().getConfig().getInt("cards." + cardName.replace(" Card", "") + ".cooldown", 60);
        } catch (Exception ignored) {}
        applyCooldown(player, cardName, sec);
    }

    public void applyCooldown(Player player, String cardName, int seconds) {
        UUID id = player.getUniqueId();
        cooldowns.putIfAbsent(id, new HashMap<>());
        long until = System.currentTimeMillis() + seconds * 1000L;
        cooldowns.get(id).put(cardName, until);
    }

    public long getRemainingSeconds(Player player, String cardName) {
        UUID id = player.getUniqueId();
        if (!cooldowns.containsKey(id)) return 0;
        Map<String, Long> map = cooldowns.get(id);
        if (!map.containsKey(cardName)) return 0;
        long left = map.get(cardName) - System.currentTimeMillis();
        return Math.max(0, left / 1000);
    }
}
