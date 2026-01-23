package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.Bukkit;
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
    
    // Har card apni unique ID yahan se dega
    public abstract int getModelData();

    /**
     * Standard card creation with NBT Tags and Glow
     */
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.PAPER); 
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            int cardID = getModelData();
            String cardName = getName();

            // --- CONSOLE DEBUG MESSAGE ---
            // Isse tumhe console mein dikhega ki card sahi ID se ban raha hai
            Bukkit.getLogger().info("§8[§6SpecialSMP-Debug§8] §aCreating Card: §e" + cardName + " §7| §aCustomModelData: §e" + cardID);

            // 1. Name
            meta.setDisplayName("§6§l" + cardName);

            // 2. Custom Model Data (Isi se texture dikhta hai)
            meta.setCustomModelData(cardID); 

            // 3. Shiny Effect
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            // 4. NBT Tag (PDC)
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, cardName);

            // Meta ko item par apply karna zaroori hai
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
                item.setItemMeta(meta); // Meta update karne ke baad apply karna zaroori hai
            }
        }
        return item;
    }
            }
