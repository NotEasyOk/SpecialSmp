package com.noteasyok.spcialsmp.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // Check karo ki kya ye Card Spin wali inventory hai
        if (e.getView().getTitle().equals("§8» §0§lCARD SPIN")) {
            
            // Player ko koi bhi item click karne se roko
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getView().getTitle().equals("§8» §0§lCARD SPIN")) {
            e.setCancelled(true);
        }
    }
}
