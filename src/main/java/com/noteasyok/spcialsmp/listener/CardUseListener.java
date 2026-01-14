package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.Card;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

        // DETECT left / right / shift-left (mapped) / shift-right
        switch (e.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                if (p.isSneaking()) {
                    // treat shift + left as shiftRightClick (we standardized)
                    card.shiftRightClick(p);
                } else {
                    card.leftClick(p);
                }
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (p.isSneaking()) {
                    card.shiftRightClick(p);
                } else {
                    card.rightClick(p);
                }
            }
            default -> {}
        }

        // cooldown handling + actionbar display
        var cdManager = SpcialSmp.get().getCooldownManager();
        // if just used, apply cooldown now (ensure canUse check earlier if you want prevention BEFORE use)
        cdManager.applyCooldown(p, display);

        // Start actionbar countdown display (runs every second)
        AtomicInteger[] taskIdHolder = new AtomicInteger[]{new AtomicInteger(-1)};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long left = cdManager.getRemainingSeconds(p, display);
                if (left <= 0) {
                    // clear actionbar and stop
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                    if (taskIdHolder[0].get() != -1) Bukkit.getScheduler().cancelTask(taskIdHolder[0].get());
                    return;
                }
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6" + display + " §7cooldown: §c" + left + "s"));
            }
        };

        int taskId = Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), runnable, 0L, 20L).getTaskId();
        taskIdHolder[0].set(taskId);
    }
}
