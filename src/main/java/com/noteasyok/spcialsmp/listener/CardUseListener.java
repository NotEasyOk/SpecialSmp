package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.cards.UnlimitedCard;
import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.Card;
import com.noteasyok.spcialsmp.manager.CooldownManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CardUseListener implements Listener {

    private final Map<String, Card> cards;
    // track running actionbar tasks so we can cancel per-player+card+action
    private final Map<String, Integer> actionBarTasks = new ConcurrentHashMap<>();

    public CardUseListener(Map<String, Card> cards) {
        this.cards = cards;
    }

    private String taskKey(UUID uuid, String card, String action) {
        return uuid.toString() + ":" + card + ":" + action;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        ItemStack it = e.getItem();
        if (it == null || !it.hasItemMeta() || !it.getItemMeta().hasDisplayName()) return;

        String display = it.getItemMeta().getDisplayName();
        if (!cards.containsKey(display)) return;

        Player p = e.getPlayer();
        Card card = cards.get(display);
        CooldownManager cd = SpcialSmp.get().getCooldownManager();
        
        if (card instanceof UnlimitedCard uc) {
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

        if (!cd.canUse(p, display, actionKey)) {
            long left = cd.getRemainingSeconds(p, display, actionKey);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c" + display + " " + actionKey + " cooldown: §e" + left + "s"));
            return;
        }

        // Execute ability
        switch (actionKey) {
            case "left" -> card.leftClick(p);
            case "right" -> card.rightClick(p);
            case "shift_right" -> card.shiftRightClick(p);
        }

        // Apply cooldown for this card+action
        cd.applyCooldown(p, display, actionKey);

        // Start / manage actionbar countdown for this player+card+action
        String tk = taskKey(p.getUniqueId(), display, actionKey);
        // cancel previous if exists
        if (actionBarTasks.containsKey(tk)) {
            Integer old = actionBarTasks.remove(tk);
            if (old != null) Bukkit.getScheduler().cancelTask(old);
        }

        int taskId = Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), new Runnable() {
            @Override
            public void run() {
                long left = cd.getRemainingSeconds(p, display, actionKey);
                if (left <= 0) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                    Integer t = actionBarTasks.remove(tk);
                    if (t != null) Bukkit.getScheduler().cancelTask(t);
                    return;
                }
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6" + display + " §7" + actionKey + " cooldown: §c" + left + "s"));
            }
        }, 0L, 20L).getTaskId();

        actionBarTasks.put(tk, taskId);
    }
}
