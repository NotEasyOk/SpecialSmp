package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class UnlimitedCraftListener implements Listener {

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        ItemStack result = e.getRecipe().getResult();
        if (!result.hasItemMeta()) return;
        if (!"Unlimited Card".equals(result.getItemMeta().getDisplayName())) return;

        Location loc = p.getLocation();

        e.setCancelled(true);

        p.getInventory().addItem(result);

        World w = p.getWorld();

        for (int i = 0; i < 30; i++) {
            int delay = i;
            Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
                w.spawnParticle(Particle.PORTAL, loc, 40, 0.8, 1, 0.8);
                w.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.5f);
            }, delay * 2L);
        }
    }
}
