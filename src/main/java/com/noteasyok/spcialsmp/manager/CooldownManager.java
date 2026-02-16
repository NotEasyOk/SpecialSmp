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
        return ChatColor.stripColor(cardName) + ":" + action.toLowerCase();
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
        String path = "cooldowns.\"" + cleanName + "\"." + action;
        long seconds = plugin.getConfig().getLong(path, 60L);
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
                
                boolean holdingCard = false;
                String cardId = null;
                if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
                    NamespacedKey cardKey = new NamespacedKey(plugin, "card_id");
                    cardId = item.getItemMeta().getPersistentDataContainer().get(cardKey, PersistentDataType.STRING);
                    if (cardId != null) holdingCard = true;
                }

                if (holdingCard) {
                    // --- CARD COOLDOWN BAR (Wapis On) ---
                    long leftCD = getRemainingSeconds(p, cardId, "left");
                    long rightCD = getRemainingSeconds(p, cardId, "right");
                    long shiftCD = getRemainingSeconds(p, cardId, "shift_right");
                    long maxCD = Math.max(leftCD, Math.max(rightCD, shiftCD));

                    if (maxCD > 0) {
                        coolingDownPlayers.add(p.getUniqueId());
                        // Ye raha tumhara purana format [||||||]
                        String message = "§6§l" + cardId + " §8[§f||||||§8] §c" + maxCD + "s";
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                    } else if (coolingDownPlayers.contains(p.getUniqueId())) {
                        // Ready Effect
                        coolingDownPlayers.remove(p.getUniqueId());
                        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getEyeLocation(), 40, 0.3, 0.3, 0.3, 0.5);
                        p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.2f);
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a§l✔ " + cardId + " READY"));
                    }
                } else {
                    // Card haath mein nahi hai toh Action bar khali rahega.
                    // Lives hunger bar ke upar HeartManager se dikhengi.
                }
            }
        }, 0L, 20L); 
    }
                    }
