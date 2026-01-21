package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Card {

    String getName();

    // Ye zaroori hai Texture ke liye!
    int getModelData(); 

    ItemStack createItem(); 

    default void leftClick(Player p) {}
    default void rightClick(Player p) {}
    default void shiftRightClick(Player p) {}
}
