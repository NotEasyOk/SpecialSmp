package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.List;

public class RevivalManager {

    public static void registerRevivalRecipe() {
        ItemStack item = getRevivalCard();
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "revival_card_recipe");
        
        // Agar recipe pehle se hai toh remove karke reload karo
        if (Bukkit.getRecipe(key) != null) Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("DCD", "TST", "DBD");
        
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('C', Material.ECHO_SHARD); 
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('S', Material.NETHER_STAR);
        recipe.setIngredient('B', Material.BEACON);

        Bukkit.addRecipe(recipe);
    }

    public static ItemStack getRevivalCard() {
        ItemStack item = new ItemStack(Material.ECHO_SHARD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§d§lREVIVAL CARD");
            List<String> lore = new ArrayList<>();
            lore.add("§7Use this to bring back a fallen soul.");
            lore.add("§eRight-Click to open Resurrection Menu.");
            meta.setLore(lore);
            
            // Tagging the item so it's special
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "is_revival_card");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void unbanPlayer(String name) {
        // Console command se unban karna zyada safe hai
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(name);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pardon " + name);
    }
        }
