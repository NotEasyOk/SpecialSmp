package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class UltimateCraftListener implements Listener {

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        ItemStack result = e.getRecipe().getResult();
        if (!result.hasItemMeta()) return;
        
        // FIXED: "Unlimited Card" ko "Ultimate Card" se badla jo aapki class mein hai
        String displayName = ChatColor.stripColor(result.getItemMeta().getDisplayName());
        if (!"Ultimate Card".equals(displayName)) return;

        Location loc = p.getLocation();

        // Crafting cancel karke materials delete karo taaki player baar-baar na le sake
        e.setCancelled(true);
        e.getInventory().setMatrix(new ItemStack[9]); // Sabhi crafting materials remove karne ke liye

        // Player ko manual item do
        p.getInventory().addItem(result);

        World w = p.getWorld();

        // Aapka particle aur sound wala logic (Same rakha hai)
        for (int i = 0; i < 30; i++) {
            int delay = i;
            Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
                w.spawnParticle(Particle.PORTAL, loc, 40, 0.8, 1, 0.8);
                w.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.5f);
            }, delay * 2L);
        }
    }
            }
