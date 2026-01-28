package com.noteasyok.spcialsmp.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class InventoryListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        
        // Sabhi custom GUIs ke titles check karo
        if (title.equals("§8» §0§lCARD SELECTION") || 
            title.equals("§0Revival Card Recipe") || 
            title.equals("§0Select Soul to Revive")) {
            
            e.setCancelled(true);
            
            // Shift-click aur Hotbar protection
            if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || 
                e.getAction() == InventoryAction.HOTBAR_SWAP ||
                e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent e) {
        String title = e.getView().getTitle();
        if (title.equals("§8» §0§lCARD SELECTION") || 
            title.equals("§0Revival Card Recipe") || 
            title.equals("§0Select Soul to Revive")) {
            e.setCancelled(true);
        }
    }
}
