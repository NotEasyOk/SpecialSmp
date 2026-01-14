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

        boolean onlyPlayerKill = SpcialSmp.get()
                .getConfig()
                .getBoolean("drops.only-player-kill", true);

        if (!onlyPlayerKill) return;
        if (killer == null) return;

        Iterator<ItemStack> it = e.getDrops().iterator();
        while (it.hasNext()) {
            ItemStack item = it.next();
            if (item == null || !item.hasItemMeta()) continue;
            if (!item.getItemMeta().hasDisplayName()) continue;

            String name = item.getItemMeta().getDisplayName();
            if (CardRegistry.getCards().containsKey(name)) {
                it.remove();
                victim.getWorld().dropItemNaturally(victim.getLocation(), item);
                break;
            }
        }
    }
}
