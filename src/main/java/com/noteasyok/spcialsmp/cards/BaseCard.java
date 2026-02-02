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

            // REMOVED: Enchantment wala shiny effect hata diya gaya hai
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getName());

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack getItemStackWithLore(String cardName) {
        // SMART FIX: Agar card ne apna custom lore (like UltimateCard) diya hai toh wahi use karega
        // Warna Registry se default lore uthayega.
        ItemStack item = createItem();
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Sirf tabhi registry se lore uthayega agar meta mein pehle se lore na ho
            if (meta.getLore() == null || meta.getLore().isEmpty()) {
                List<String> lore = CardRegistry.getDescriptionLore(cardName);
                if (lore != null && !lore.isEmpty()) {
                    meta.setLore(lore);
                }
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
