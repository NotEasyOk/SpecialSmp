package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final SpcialSmp plugin;
    // UUID -> (key -> endTimeMillis)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CooldownManager(SpcialSmp plugin) {
        this.plugin = plugin;
    }

    private String key(String cardName, String action) {
        return cardName + ":" + action;
    }

    public boolean canUse(Player player, String cardName, String action) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return true;
        Long t = map.get(key(cardName, action));
        if (t == null) return true;
        return System.currentTimeMillis() >= t;
    }

    public void applyCooldown(Player player, String cardName, String action) {
        long seconds = plugin.getConfig().getLong("cooldowns." + cardName + "." + action,
                plugin.getConfig().getLong("cooldown-seconds", 60));
        long end = System.currentTimeMillis() + seconds * 1000L;
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(key(cardName, action), end);
    }

    public long getRemainingSeconds(Player player, String cardName, String action) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return 0;
        Long t = map.get(key(cardName, action));
        if (t == null) return 0;
        long left = t - System.currentTimeMillis();
        return Math.max(0, left / 1000);
    }
}
