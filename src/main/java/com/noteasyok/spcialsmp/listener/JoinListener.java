package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Player data manager se check karna
        var data = SpcialSmp.get().getPlayerDataManager();

        // 1. Agar player pehle hi card le chuka hai toh ruk jao
        if (data.hasReceivedFirstCard(p.getUniqueId())) {
            return;
        }

        // 2. Registry se random card lena
        ItemStack card = CardRegistry.getRandomCard();

        // YAHAN MISTAKE THI: Agar card null hai, toh plugin kuch nahi karta
        if (card == null) {
            // Debug ke liye console mein message (optional)
            SpcialSmp.get().getLogger().warning("CardRegistry returned null for player: " + p.getName());
            return;
        }

        // 3. Inventory mein add karna
        p.getInventory().addItem(card);

        // 4. Data save karna taki dobara na mile
        String cardName = card.hasItemMeta() && card.getItemMeta().hasDisplayName() 
                          ? card.getItemMeta().getDisplayName() 
                          : "Unknown Card";
        
        data.setReceivedFirstCard(p.getUniqueId(), cardName);

        // 5. Player ko message bhejna
        p.sendMessage("§aYou received your first card!");
    }
}
