package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CooldownManager {

    private final SpcialSmp plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Set<UUID> coolingDownPlayers = new HashSet<>();

    public CooldownManager(SpcialSmp plugin) {
        this.plugin = plugin;
        startDisplayTask();
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
                
                // Agar card haath mein nahi hai toh display nahi dikhega
                if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) continue;
                
                NamespacedKey cardKey = new NamespacedKey(plugin, "card_id");
                String cardId = item.getItemMeta().getPersistentDataContainer().get(cardKey, PersistentDataType.STRING);
                if (cardId == null) continue;

                long leftCD = getRemainingSeconds(p, cardId, "left");
                long rightCD = getRemainingSeconds(p, cardId, "right");
                long shiftCD = getRemainingSeconds(p, cardId, "shift_right");

                long maxCD = Math.max(leftCD, Math.max(rightCD, shiftCD));

                if (maxCD > 0) {
                    // 1. CARD COOLDOWN: Soul Fuel Hide
                    coolingDownPlayers.add(p.getUniqueId());
                    String bar = "§8[§f||||||§8]"; 
                    String message = "§6§l" + cardId + " " + bar + " §c" + maxCD + "s";
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                
                } else {
                    // 2. READY: Totem Effect aur Sound chalega jab cooldown khatam ho
                    if (coolingDownPlayers.contains(p.getUniqueId())) {
                        coolingDownPlayers.remove(p.getUniqueId());
                        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getEyeLocation(), 40, 0.3, 0.3, 0.3, 0.5);
                        p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.2f);
                    }

                    // 3. SOUL FUEL: Fixed 23h 59m Logic (Ready text hat gaya)
                    long dayMillis = 24 * 60 * 60 * 1000L; 
                    long remainingMillis = dayMillis - (System.currentTimeMillis() % dayMillis);

                    long hours = (remainingMillis / 3600000) % 24;
                    long minutes = (remainingMillis / 60000) % 60;
                    long seconds = (remainingMillis / 1000) % 60;

                    String timeStr = String.format("%02dh %02dm %02ds", hours, minutes, seconds);
                    
                    // Yahan se READY hat gaya hai
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                        new TextComponent("§b§lSOUL FUEL: §f" + timeStr));
                }
            }
        }, 0L, 10L); 
    }
    }
