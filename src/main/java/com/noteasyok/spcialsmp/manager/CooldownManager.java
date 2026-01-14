package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final SpcialSmp plugin;

    // player -> (cardName -> endTime)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CooldownManager(SpcialSmp plugin) {
        this.plugin = plugin;
    }

    public boolean canUse(Player player, String card) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return true;
        if (!map.containsKey(card)) return true;

        return System.currentTimeMillis() >= map.get(card);
    }

    public void applyCooldown(Player player, String card) {

        long seconds = plugin.getConfig()
                .getLong("cooldowns." + card,
                        plugin.getConfig().getLong("cooldown-seconds", 60));

        long endTime = System.currentTimeMillis() + (seconds * 1000);

        cooldowns
                .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(card, endTime);
    }

    public long getRemainingSeconds(Player player, String card) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return 0;
        if (!map.containsKey(card)) return 0;

        long diff = map.get(card) - System.currentTimeMillis();
        return Math.max(0, diff / 1000);
    }
}
