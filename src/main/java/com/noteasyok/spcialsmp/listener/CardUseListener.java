package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.Card;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CardUseListener implements Listener {

    private final Map<String, Card> cards;

    public CardUseListener(Map<String, Card> cards) {
        this.cards = cards;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getItem() == null) return;

        ItemStack item = e.getItem();
        if (!item.hasItemMeta()) return;
        if (!item.getItemMeta().hasDisplayName()) return;

        String name = item.getItemMeta().getDisplayName();
        if (!cards.containsKey(name)) return;

        Player p = e.getPlayer();

        // cooldown check
        if (!SpcialSmp.get().getCooldownManager().canUse(p, name)) {
            p.sendMessage("§cCard cooldown active!");
            return;
        }

        Card card = cards.get(name);

        switch (e.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                card.leftClick(p);
                break;

            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                if (p.isSneaking()) {
                    card.shiftRightClick(p);
                } else {
                    card.rightClick(p);
                }
                break;
            default:
                break;
        }

        SpcialSmp.get().getCooldownManager().applyCooldown(p, name);
    }
            }
          
