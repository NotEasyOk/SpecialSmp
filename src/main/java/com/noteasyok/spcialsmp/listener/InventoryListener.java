package com.noteasyok.spcialsmp.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent e) {
        // Precise Title Check
        String title = e.getView().getTitle();
        
        if (title.equals("§8» §0§lCARD SELECTION")) {
            // Saare clicks cancel karo
            e.setCancelled(true);
            
            // Extra Security: Agar player niche wali inventory (apni inventory) se 
            // item shift-click karke upar laane ki koshish kare, toh use bhi roko
            if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || 
                e.getAction() == InventoryAction.HOTBAR_SWAP ||
                e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getView().getTitle().equals("§8» §0§lCARD SELECTION")) {
            e.setCancelled(true);
        }
    }
}
