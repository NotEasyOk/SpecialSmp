package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public abstract class BaseCard {

    public abstract String getName();
    public abstract void leftClick(Player p);
    public abstract void rightClick(Player p);
    public abstract void shiftRightClick(Player p);

    // ✅ FIX: Item Creation logic with Identity Tag
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.PAPER); // Ya jo bhi material ho
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // 1. Display Name Set karo
            meta.setDisplayName("§6§l" + getName());

            // 2. Custom Model Data (Agar texture use kar rahe ho)
            meta.setCustomModelData(1); 

            // 3. Shiny Effect (Enchantment Glow)
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            // 4. IMPORTANT: NBT Tag lagana (Iske bina ability kaam nahi karegi)
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getName());

            item.setItemMeta(meta);
        }
        return item;
    }
}
