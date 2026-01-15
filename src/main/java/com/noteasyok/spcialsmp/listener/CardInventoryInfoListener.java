package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CardInventoryInfoListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player p)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        if (!item.getItemMeta().hasDisplayName()) return;

        String name = item.getItemMeta().getDisplayName();

        // Agar card hai
        if (!CardRegistry.getCards().containsKey(name)) return;

        // Inventory me click hone par power use NA ho
        e.setCancelled(true);

        // Card info dikhao
        List<String> lore = CardRegistry.getDescriptionLore(name);

        p.sendMessage("§a=== " + name + " Info ===");
        for (String line : lore) {
            p.sendMessage("§7• " + line);
        }
    }
}
