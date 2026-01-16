package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        var data = SpcialSmp.get().getPlayerDataManager();

        // ✅ already received → return
        if (data.hasReceivedFirstCard(p.getUniqueId())) return;

        // ✅ random card from registry
        ItemStack card = CardRegistry.getRandomCard();
        if (card == null) return;

        p.getInventory().addItem(card);

        data.setReceivedFirstCard(
                p.getUniqueId(),
                card.getItemMeta().getDisplayName()
        );

        p.sendMessage("§aYou received your first card!");
    }
}
