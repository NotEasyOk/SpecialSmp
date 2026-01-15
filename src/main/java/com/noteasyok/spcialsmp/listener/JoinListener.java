package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class JoinListener implements Listener {

    private final List<ItemStack> cards;

    public JoinListener() {
        
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        var data = SpcialSmp.get().getPlayerDataManager();

        if (data.hasReceivedFirstCard(p.getUniqueId())) return;

        ItemStack randomCard = cards.get(new Random().nextInt(cards.size()));
        p.getInventory().addItem(randomCard);

        data.setReceivedFirstCard(
                p.getUniqueId(),
                randomCard.getItemMeta().getDisplayName()
        );

        p.sendMessage("§aYou received your first random card!");
        Bukkit.getLogger().info(p.getName() + " received first card");
    }
}
