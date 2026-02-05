package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CooldownManager {

    private final SpcialSmp plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    // Ye set track karega ki kaun abhi cooldown mein hai taaki animation play kar sakein
    private final Set<UUID> coolingDownPlayers = new HashSet<>();

    public CooldownManager(SpcialSmp plugin) {
        this.plugin = plugin;
        startDisplayTask(); // ActionBar task start
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
                
                if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) continue;
                
                NamespacedKey cardKey = new NamespacedKey(plugin, "card_id");
                if (!item.getItemMeta().getPersistentDataContainer().has(cardKey, PersistentDataType.STRING)) continue;
                
                String cardId = item.getItemMeta().getPersistentDataContainer().get(cardKey, PersistentDataType.STRING);
                if (cardId == null) continue;

                // Cooldowns check
                long leftCD = getRemainingSeconds(p, cardId, "left");
                long rightCD = getRemainingSeconds(p, cardId, "right");
                long shiftCD = getRemainingSeconds(p, cardId, "shift_right");

                boolean isOnCooldown = (leftCD > 0 || rightCD > 0 || shiftCD > 0);

                if (isOnCooldown) {
                    // Agar cooldown chal raha hai, player ko tracking list mein dalo
                    coolingDownPlayers.add(p.getUniqueId());

                    // Soul Fuel HIDE, Sirf Timer dikhao
                    StringBuilder sb = new StringBuilder("§c§l" + cardId.toUpperCase() + " §8» ");
                    if (leftCD > 0) sb.append("§eL: §f").append(leftCD).append("s ");
                    if (rightCD > 0) sb.append("§eR: §f").append(rightCD).append("s ");
                    if (shiftCD > 0) sb.append("§6S+R: §f").append(shiftCD).append("s");
                    
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(sb.toString()));
                
                } else {
                    // Agar cooldown abhi abhi khatam hua hai (Player list mein tha par ab timer 0 hai)
                    if (coolingDownPlayers.contains(p.getUniqueId())) {
                        coolingDownPlayers.remove(p.getUniqueId());
                        
                        // === TOTEM POP ANIMATION & SOUND ===
                        // Ye sirf tab play hoga jab cooldown khatam hoga
                        p.getWorld().spawnParticle(Particle.TOTEM, p.getEyeLocation(), 40, 0.3, 0.3, 0.3, 0.5);
                        p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.2f);
                        p.sendMessage("§a§lABILITIES READY!"); // Optional message
                    }

                    // Soul Fuel WAPIS dikhao
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§b§lSOUL FUEL: §f100% §8| §a§lREADY"));
                }
            }
        }, 0L, 5L); // Thoda fast update (0.25s) taaki animation smooth lage
    }
            }
