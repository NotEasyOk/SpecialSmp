package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class BaseCard {

    public abstract String getName();

    public void leftClick(Player p) {}
    public void rightClick(Player p) {}
    public void shiftRightClick(Player p) {}

    // agar registry use kar raha hai
    public ItemStack createItem() {
        return null;
    }
}
