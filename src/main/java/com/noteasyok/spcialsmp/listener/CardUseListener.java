package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.manager.CooldownManager;
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

public class CardUseListener implements Listener {

    private final Map<String, BaseCard> cards;

    public CardUseListener(Map<String, BaseCard> cards) {
        this.cards = cards;
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

        // Agar cooldown hai toh yahan se return ho jayega
        // Display ab CooldownManager ka task handle karega
        if (!cd.canUse(p, cardID, actionKey)) {
            return; 
        }

        switch (actionKey) {
            case "left" -> card.leftClick(p);
            case "right" -> card.rightClick(p);
            case "shift_right" -> card.shiftRightClick(p);
        }

        cd.applyCooldown(p, cardID, actionKey);
        // startActionBarCountdown yahan se hata diya gaya hai taaki flickering na ho
    }
        }
