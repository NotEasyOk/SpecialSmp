package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Card {
    
    String getName();
    
    void leftClick(Player p);
    void rightClick(Player p);
    void shiftRightClick(Player p);
    
    ItemStack createItem();
 }
