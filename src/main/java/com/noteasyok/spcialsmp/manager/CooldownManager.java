package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final SpcialSmp plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CooldownManager(SpcialSmp plugin) {
        this.plugin = plugin;
        startDisplayTask(); // ActionBar ko update karne wala task
    }

    private String key(String cardName, String action) {
        return ChatColor.stripColor(cardName).toLowerCase() + ":" + action.toLowerCase();
    }

    public boolean canUse(Player player, String cardName, String action) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return true;
        Long t = map.get(key(cardName, action));
        if (t == null) return true;
        return System.currentTimeMillis() >= t;
    }

    public void applyCooldown(Player player, String cardName, String action) {
        String cleanName = ChatColor.stripColor(cardName);
        String path = "cooldowns." + cleanName + "." + action;
        long seconds = plugin.getConfig().getLong(path, plugin.getConfig().getLong("cooldown-seconds", 60));

        if (seconds <= 0) return;

        long end = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(key(cleanName, action), end);
    }

    public long getRemainingSeconds(Player player, String cardName, String action) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return 0;
        Long t = map.get(key(cardName, action));
        if (t == null) return 0;
        return Math.max(0, (t - System.currentTimeMillis()) / 1000);
    }

    /* ================= ACTION BAR DISPLAY LOGIC ================= */
    private void startDisplayTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                ItemStack item = p.getInventory().getItemInMainHand();
                
                // Check if it's a Card
                if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) continue;
                
                NamespacedKey cardKey = new NamespacedKey(plugin, "card_id");
                String cardId = item.getItemMeta().getPersistentDataContainer().get(cardKey, PersistentDataType.STRING);
                
                if (cardId == null) continue;

                // Check cooldowns for Left, Right, and Shift-Right
                long leftCD = getRemainingSeconds(p, cardId, "left");
                long rightCD = getRemainingSeconds(p, cardId, "right");
                long shiftCD = getRemainingSeconds(p, cardId, "shift_right");

                if (leftCD > 0 || rightCD > 0 || shiftCD > 0) {
                    // COOLDOWN ACTIVE: Soul Fuel Hide
                    StringBuilder sb = new StringBuilder("§c§l" + cardId.toUpperCase() + " §8» ");
                    if (leftCD > 0) sb.append("§eL: §f").append(leftCD).append("s ");
                    if (rightCD > 0) sb.append("§eR: §f").append(rightCD).append("s ");
                    if (shiftCD > 0) sb.append("§eS+R: §f").append(shiftCD).append("s");
                    
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(sb.toString()));
                } else {
                    // NO COOLDOWN: Show Soul Fuel (Hide Cooldown)
                    // Yahan aap apna Soul Fuel variable replace kar sakte hain
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§b§lSOUL FUEL: §f100% §8| §a§lREADY"));
                }
            }
        }, 0L, 10L); // Har 0.5 sec mein update hoga
    }
            }
