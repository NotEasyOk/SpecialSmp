package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
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

    /**
     * Standard card creation with NBT Tags and Glow
     */
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.PAPER); 
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // 1. Name
            meta.setDisplayName("§6§l" + getName());

            // 2. Custom Model Data
            meta.setCustomModelData(1); 

            // 3. Shiny Effect (Fix: 1.21 uses UNBREAKING instead of DURABILITY)
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            // 4. NBT Tag (PDC) - Essential for detection
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getName());

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Helper method for the Spinner and Registry to add Lore
     */
    public ItemStack getItemStackWithLore(String cardName) {
        ItemStack item = createItem();
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            List<String> lore = CardRegistry.getDescriptionLore(cardName);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
