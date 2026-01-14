package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.Card;
import com.noteasyok.spcialsmp.manager.CooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Map;

public class CardUseListener implements Listener {

    private final Map<String, Card> cards;

    public CardUseListener(Map<String, Card> cards) {
        this.cards = cards;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {

        ItemStack item = e.getItem();
        if (item == null || !item.hasItemMeta()) return;
        if (!item.getItemMeta().hasDisplayName()) return;

        String display = item.getItemMeta().getDisplayName();
        if (!cards.containsKey(display)) return;

        Player p = e.getPlayer();
        Card card = cards.get(display);
        CooldownManager cd = SpcialSmp.get().getCooldownManager();

        e.setCancelled(true); // important

        // cooldown check FIRST
        if (!cd.canUse(p, display)) {
            long left = cd.getRemainingSeconds(p, display);
            p.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    new TextComponent("§c" + display + " cooldown: §e" + left + "s")
            );
            return;
        }

        Action action = e.getAction();

        // ===== Ability trigger =====
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            card.leftClick(p);
        }

        else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (p.isSneaking()) {
                card.shiftRightClick(p);
            } else {
                card.rightClick(p);
            }
        }

        // apply cooldown AFTER successful use
        cd.applyCooldown(p, display);

        startCooldownBar(p, display, cd);
    }

    private void startCooldownBar(Player p, String card, CooldownManager cd) {

        int taskId = Bukkit.getScheduler().runTaskTimer(
                SpcialSmp.get(),
                () -> {
                    long left = cd.getRemainingSeconds(p, card);
                    if (left <= 0) {
                        p.spigot().sendMessage(
                                ChatMessageType.ACTION_BAR,
                                new TextComponent("")
                        );
                        Bukkit.getScheduler().cancelTasks(SpcialSmp.get());
                        return;
                    }
                    p.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            new TextComponent("§6" + card + " §7cooldown: §c" + left + "s")
                    );
                },
                0L,
                20L
        ).getTaskId();
    }
}
