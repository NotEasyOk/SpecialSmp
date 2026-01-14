package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.Card;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

private final Map<String, Card> cardMap;

public CardUseListener(Map<String, Card> cardMap) {
    this.cardMap = cardMap;
}

    @EventHandler
    public void onUse(PlayerInteractEvent e) {

        if (e.getItem() == null || !e.getItem().hasItemMeta()) return;

        String name = e.getItem().getItemMeta().getDisplayName();
        if (!cardMap.containsKey(name)) return;

        Player p = e.getPlayer();
        Card card = cardMap.get(name);

        var cooldown = SpcialSmp.get().getCooldownManager();

        if (cooldown.onCooldown(p.getUniqueId(), card.getName())) {
            long left = cooldown.timeLeft(p.getUniqueId(), card.getName());
            p.sendMessage("§cCooldown: " + left + "s");
            return;
        }

        if (p.isSneaking()) {
            card.shiftRightClick(p);
        } else {
            switch (e.getAction()) {
                case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> card.leftClick(p);
                case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> card.rightClick(p);
            }
        }

        cooldown.set(p.getUniqueId(), card.getName(), 60);
    }
            }
