package com.noteasyok.spcialsmp.cards;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCard implements Card {

    @Override
    public abstract String getName();

    @Override
    public void leftClick(Player p) {}

    @Override
    public void rightClick(Player p) {}

    @Override
    public void shiftRightClick(Player p) {}

    // ✅ FIXED: Ab ye null return nahi karega, balki ek real item banayega
    @Override
    public ItemStack createItem() {
        // Aap yahan Material change kar sakte hain (e.g., PAPER, NETHER_STAR)
        ItemStack item = new ItemStack(Material.PAPER); 
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Card ka naam set karna (Colors ke saath)
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + getName());
            
            // Lore (Optional: Aap isme description bhi add kar sakte hain)
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Special Ability Card");
            meta.setLore(lore);
            
            item.setItemMeta(meta);
        }

        return item;
    }
}
