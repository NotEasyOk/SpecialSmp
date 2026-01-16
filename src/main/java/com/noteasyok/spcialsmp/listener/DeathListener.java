package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.ChatColor; // Ise import karna zaroori hai
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class DeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity();
        Player killer = dead.getKiller();

        // ✅ Agar killer player hai toh cards drop hone do
        if (killer != null) {
            return;
        }

        // ❌ Natural death (killer == null) -> Saare cards remove karo
        Iterator<ItemStack> it = e.getDrops().iterator();

        while (it.hasNext()) {
            ItemStack item = it.next();

            if (item == null || !item.hasItemMeta()) continue;
            if (!item.getItemMeta().hasDisplayName()) continue;

            // ChatColor.stripColor use kiya taaki '§6Ultimate Card' sirf 'Ultimate Card' ban jaye
            String displayName = item.getItemMeta().getDisplayName();
            String cleanName = ChatColor.stripColor(displayName);

            // Ab Registry se check karo
            if (CardRegistry.getCards().containsKey(cleanName)) {
                it.remove();
            }
        }
    }
}
