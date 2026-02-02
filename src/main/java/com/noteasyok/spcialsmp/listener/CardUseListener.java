package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.cards.UltimateCard;
import com.noteasyok.spcialsmp.manager.CooldownManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CardUseListener implements Listener {

    private final Map<String, BaseCard> cards;
    private final Map<String, Integer> actionBarTasks = new ConcurrentHashMap<>();

    public CardUseListener(Map<String, BaseCard> cards) {
        this.cards = cards;
    }

    private String taskKey(UUID uuid, String card, String action) {
        return uuid.toString() + ":" + card + ":" + action;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUse(PlayerInteractEvent e) {
        ItemStack it = e.getItem();
        if (it == null || !it.hasItemMeta()) return;

        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        String cardID = it.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        
        if (cardID == null || !cards.containsKey(cardID)) return;

        Player p = e.getPlayer();

        if (cardID.equals("Ultimate Card") && it.getType() == Material.WARPED_FUNGUS_ON_A_STICK) {
            return; 
        }

        BaseCard card = cards.get(cardID);
        CooldownManager cd = SpcialSmp.get().getCooldownManager();
        
        e.setCancelled(true);

        String actionKey;
        Action a = e.getAction();
        if ((a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) && p.isSneaking()) {
            actionKey = "shift_right";
        } else if (a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK) {
            actionKey = "left";
        } else if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
            actionKey = "right";
        } else {
            return;
        }

        if (!cd.canUse(p, cardID, actionKey)) {
            return; // Action bar niche wale task se handle hogi
        }

        switch (actionKey) {
            case "left" -> card.leftClick(p);
            case "right" -> card.rightClick(p);
            case "shift_right" -> card.shiftRightClick(p);
        }

        cd.applyCooldown(p, cardID, actionKey);
        startActionBarCountdown(p, cardID, actionKey, cd);
    }

    private void startActionBarCountdown(Player p, String cardID, String actionKey, CooldownManager cd) {
        String tk = taskKey(p.getUniqueId(), cardID, actionKey);
        
        if (actionBarTasks.containsKey(tk)) {
            Bukkit.getScheduler().cancelTask(actionBarTasks.remove(tk));
        }

        int taskId = Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), () -> {
            if (!p.isOnline()) {
                actionBarTasks.remove(tk);
                return;
            }
            long left = cd.getRemainingSeconds(p, cardID, actionKey);
            
            if (left <= 0) {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a§l✔ §f" + cardID + " §a§lREADY!"));
                Bukkit.getScheduler().cancelTask(actionBarTasks.remove(tk));
                return;
            }

            // --- CUSTOM DESIGN: CARD NAME [||||||] TIME ---
            // Yahan humne design ko simple rakha hai taaki Soul Fuel ke paas sahi dikhe
            String bar = "§8[§f||||||§8]"; 
            String message = "§e" + cardID + " " + bar + " §c" + left + "s";
            
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));

        }, 0L, 20L).getTaskId();

        actionBarTasks.put(tk, taskId);
    }
            }
