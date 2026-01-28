package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class RevivalManager {

    public static void registerRevivalRecipe() {
        // Revival Item banana
        ItemStack revivalItem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = revivalItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§d§lSOUL REVIVAL CARD");
            List<String> lore = new ArrayList<>();
            lore.add("§7Use this to revive a dead soul.");
            lore.add("§eHold and Right-Click to open unban menu.");
            meta.setLore(lore);
            revivalItem.setItemMeta(meta);
        }

        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "revival_card");
        ShapedRecipe recipe = new ShapedRecipe(key, revivalItem);

        /* Recipe as per your request:
           D C D  (Diamond Block - Any Card - Diamond Block)
           T S T  (Totem - Nether Star/Soul - Totem)
           D B D  (Diamond Block - Beacon/Any - Diamond Block)
        */
        recipe.shape("DCD", "TST", "DBD");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('C', Material.PAPER); // Represents Any Card
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('S', Material.NETHER_STAR);
        recipe.setIngredient('B', Material.BEACON);

        Bukkit.addRecipe(recipe);
    }
                             }
