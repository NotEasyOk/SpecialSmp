package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class RevivalManager {

    public static void registerRevivalRecipe() {
        ItemStack item = getRevivalCard();
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "revival_card");
        
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        
        // Aapki batayi hui recipe:
        // D C D (Diamond Block - Any Card/Paper - Diamond Block)
        // T S T (Totem - Nether Star - Totem)
        // D B D (Diamond Block - Beacon - Diamond Block)
        recipe.shape("DCD", "TST", "DBD");
        
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('C', Material.PAPER); // Ye Card ko represent karega
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
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void unbanPlayer(String name) {
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(name);
    }
    }
