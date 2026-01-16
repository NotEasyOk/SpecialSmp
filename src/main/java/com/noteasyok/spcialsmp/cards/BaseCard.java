package com.noteasyok.spcialsmp.cards;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class BaseCard implements Card {

    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(getName());
        item.setItemMeta(meta);

        return item;
    }
}
