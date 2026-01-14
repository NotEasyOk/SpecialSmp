package com.noteasyok.spcialsmp.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RecipeManager {

    public static void registerUnlimitedRecipe() {

        ItemStack unlimited = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = unlimited.getItemMeta();
        meta.setDisplayName("Unlimited Card");
        unlimited.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(
                Bukkit.getPluginManager().getPlugin("spcialSmp"),
                "unlimited_card");

        ShapedRecipe recipe = new ShapedRecipe(key, unlimited);
        recipe.shape("ABC", "DEF", "GHI");

        recipe.setIngredient('A', Material.PAPER);
        recipe.setIngredient('B', Material.PAPER);
        recipe.setIngredient('C', Material.PAPER);
        recipe.setIngredient('D', Material.PAPER);
        recipe.setIngredient('E', Material.PAPER);
        recipe.setIngredient('F', Material.PAPER);
        recipe.setIngredient('G', Material.PAPER);
        recipe.setIngredient('H', Material.PAPER);
        recipe.setIngredient('I', Material.PAPER);

        Bukkit.addRecipe(recipe);
    }
  }

