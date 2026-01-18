package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.ChatColor;
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

    // Helper to generate key (Color strip yahan bhi zaroori hai)
    private String key(String cardName, String action) {
        return ChatColor.stripColor(cardName) + ":" + action;
    }

    public boolean canUse(Player player, String cardName, String action) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return true;
        
        // Color strip karke key check karo
        Long t = map.get(key(cardName, action));
        if (t == null) return true;
        
        return System.currentTimeMillis() >= t;
    }

    public void applyCooldown(Player player, String cardName, String action) {
        // 1. Naam saaf karo (Colors hatao) taaki Config key se match kare
        String cleanName = ChatColor.stripColor(cardName);
        
        // 2. Config Path Banao
        String path = "cooldowns." + cleanName + "." + action;

        long seconds;
        
        // 3. Check: Kya Config mein ye setting exist karti hai?
        if (plugin.getConfig().contains(path)) {
            seconds = plugin.getConfig().getLong(path);
        } else {
            // Agar config mein nahi mila, tabhi default 60s uthao
            seconds = plugin.getConfig().getLong("cooldown-seconds", 60);
            // Console mein batao ki config nahi mila (Debugging)
            System.out.println("[SpecialSMP] Warning: Config path not found: '" + path + "'. Using default cooldown.");
        }

        // 4. ✅ SPAM FIX: Agar cooldown 0 ya negative hai, toh save mat karo
        if (seconds <= 0) {
            return; 
        }

        long end = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(key(cleanName, action), end);
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
