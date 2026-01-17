package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.cards.UltimateCard;
import com.noteasyok.spcialsmp.manager.CooldownManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CardUseListener implements Listener {

    // ✅ FIXED: Using BaseCard instead of Card
    private final Map<String, BaseCard> cards;
    private final Map<String, Integer> actionBarTasks = new ConcurrentHashMap<>();

    public CardUseListener(Map<String, BaseCard> cards) {
        this.cards = cards;
    }

    private String taskKey(UUID uuid, String card, String action) {
        return uuid.toString() + ":" + card + ":" + action;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        ItemStack it = e.getItem();
        if (it == null || !it.hasItemMeta()) return;

        // ✅ FIXED: Display name ki jagah NBT Tag (card_id) se card pehchano
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        String cardID = it.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        
        if (cardID == null || !cards.containsKey(cardID)) return;

        Player p = e.getPlayer();
        BaseCard card = cards.get(cardID);
        CooldownManager cd = SpcialSmp.get().getCooldownManager();
        
        // Ultimate Orbit Check
        if (card instanceof UltimateCard uc) {
           uc.startOrbit(p);
        }
        
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

        // Cooldown Check
        if (!cd.canUse(p, cardID, actionKey)) {
            long left = cd.getRemainingSeconds(p, cardID, actionKey);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c" + cardID + " " + actionKey + " cooldown: §e" + left + "s"));
            return;
        }

        // Execute Ability
        switch (actionKey) {
            case "left" -> card.leftClick(p);
            case "right" -> card.rightClick(p);
            case "shift_right" -> card.shiftRightClick(p);
        }

        // Apply Cooldown
        cd.applyCooldown(p, cardID, actionKey);

        // Actionbar Countdown
        String tk = taskKey(p.getUniqueId(), cardID, actionKey);
        if (actionBarTasks.containsKey(tk)) {
            Integer old = actionBarTasks.remove(tk);
            if (old != null) Bukkit.getScheduler().cancelTask(old);
        }

        int taskId = Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), () -> {
            long left = cd.getRemainingSeconds(p, cardID, actionKey);
            if (left <= 0) {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                Integer t = actionBarTasks.remove(tk);
                if (t != null) Bukkit.getScheduler().cancelTask(t);
                return;
            }
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6" + cardID + " §7" + actionKey + " cooldown: §c" + left + "s"));
        }, 0L, 20L).getTaskId();

        actionBarTasks.put(tk, taskId);
    }
    }
