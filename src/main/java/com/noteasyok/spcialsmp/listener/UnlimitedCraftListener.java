package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class UnlimitedCraftListener implements Listener {

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getRecipe() == null || e.getCurrentItem() == null) return;

        ItemStack result = e.getRecipe().getResult();

        if (!result.hasItemMeta()) return;
        if (!result.getItemMeta().getDisplayName().equals("Unlimited Card")) return;

        var p = (org.bukkit.entity.Player) e.getWhoClicked();
        var data = SpcialSmp.get().getPlayerDataManager();

        if (data.hasUnlimited(p.getUniqueId())) {
            e.setCancelled(true);
            p.sendMessage("§cUnlimited Card sirf ek baar craft ho sakta hai!");
            return;
        }

        data.setUnlimited(p.getUniqueId());
        p.sendMessage("§aUnlimited Card successfully crafted!");
    }
}

