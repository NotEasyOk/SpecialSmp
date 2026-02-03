package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCard {

    public abstract String getName();
    public abstract void leftClick(Player p);
    public abstract void rightClick(Player p);
    public abstract void shiftRightClick(Player p);
    public abstract Material getMaterial();
    public abstract int getModelData();

    public ItemStack createItem() {
        ItemStack item = new ItemStack(getMaterial()); 
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§l" + getName());
            meta.setCustomModelData(getModelData()); 

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_DESTROYS); // Safety flags

            // Persistent Data fix: Use a standard identifier
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getName());

            item.setItemMeta(meta);
        }
        return item;
    }

    // Is method ko abstract nahi rakha hai taaki common cards registry use karein, 
    // lekin Ultimate Card isse Override kar sake bina kisi logic conflict ke.
    public ItemStack getItemStackWithLore(String cardName) {
        ItemStack item = createItem();
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Priority Check: Pehle Registry se lore uthao
            List<String> registryLore = CardRegistry.getDescriptionLore(cardName);
            List<String> finalLore = new ArrayList<>();
            
            if (registryLore != null && !registryLore.isEmpty()) {
                finalLore.addAll(registryLore);
            }
            
            // Agar extra meta attributes hain (like Unbreakable), toh wo yahan set honge
            meta.setLore(finalLore);
            item.setItemMeta(meta);
        }
        return item;
    }
    }
