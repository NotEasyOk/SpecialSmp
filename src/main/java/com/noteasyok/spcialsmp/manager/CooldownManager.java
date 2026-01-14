package com.noteasyok.spcialsmp.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    // Check cooldown
    public boolean canUse(Player player, String cardName) {
        UUID uuid = player.getUniqueId();

        if (!cooldowns.containsKey(uuid)) return true;
        if (!cooldowns.get(uuid).containsKey(cardName)) return true;

        long expireTime = cooldowns.get(uuid).get(cardName);
        return System.currentTimeMillis() > expireTime;
    }

    // Apply cooldown (default 60 seconds)
    public void applyCooldown(Player player, String cardName) {
        UUID uuid = player.getUniqueId();

        cooldowns.putIfAbsent(uuid, new HashMap<>());

        long cooldownTime = System.currentTimeMillis() + (60 * 1000);
        cooldowns.get(uuid).put(cardName, cooldownTime);
    }
}
