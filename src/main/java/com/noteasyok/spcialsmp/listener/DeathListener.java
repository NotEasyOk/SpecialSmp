package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        boolean dropOnlyIfPlayer = SpcialSmp.get().getConfig()
                .getBoolean("drops.only-player-kill", true);

        // If config requires only player kill, then remove card items from drops when killer == null
        if (dropOnlyIfPlayer && killer == null) {
            Iterator<ItemStack> it = e.getDrops().iterator();
            while (it.hasNext()) {
                ItemStack item = it.next();
                if (item == null || !item.hasItemMeta()) continue;
                String name = item.getItemMeta().getDisplayName();
                if (name != null && CardRegistry.getCards().containsKey(name)) {
                    it.remove(); // remove card from drops
                }
            }
        }

        // If you want to drop card only when killed by player, nothing else needed (cards stay in inventory otherwise)
    }
}
