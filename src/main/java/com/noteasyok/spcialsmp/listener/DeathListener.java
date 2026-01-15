package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.manager.CardRegistry;
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

        // ✅ Agar player ne player ko maara → allow card drop
        if (killer != null) {
            return;
        }

        // ❌ Natural death → REMOVE ALL CARD DROPS
        Iterator<ItemStack> it = e.getDrops().iterator();

        while (it.hasNext()) {
            ItemStack item = it.next();

            if (item == null || !item.hasItemMeta()) continue;
            if (!item.getItemMeta().hasDisplayName()) continue;

            String name = item.getItemMeta().getDisplayName();

            // Agar item card hai → remove
            if (CardRegistry.getCards().containsKey(name)) {
                it.remove();
            }
        }
    }
}
