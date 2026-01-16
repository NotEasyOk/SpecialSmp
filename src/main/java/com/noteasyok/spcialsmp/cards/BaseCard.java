package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// Yahan "implements Card" add kiya gaya hai
public abstract class BaseCard implements Card {

    public abstract String getName();

    public void leftClick(Player p) {}
    public void rightClick(Player p) {}
    public void shiftRightClick(Player p) {}

    // Item banane ka logic (Aap ise override bhi kar sakte hain)
    public ItemStack createItem() {
        return null;
    }
}
