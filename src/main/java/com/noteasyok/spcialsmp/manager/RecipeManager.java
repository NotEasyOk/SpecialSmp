package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.CardRegistry;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class RecipeManager {

    public static void registerAllRecipes(SpcialSmp plugin) {
        registerUltimateRecipe(plugin);
        registerReviveRecipe(plugin); // Ye missing tha
    }

    public static void registerUltimateRecipe(SpcialSmp plugin) {
        // ✅ FIX: Get the actual Ultimate Card from Registry (with NBT & Paper texture)
        ItemStack ultimateItem = CardRegistry.getCards().get("Ultimate Card").getItemStackWithLore("Ultimate Card");

        NamespacedKey key = new NamespacedKey(plugin, "ultimate_card_recipe");

        ShapedRecipe recipe = new ShapedRecipe(key, ultimateItem);
        recipe.shape("ABC", "DEF", "GHI");

        // Assuming A-I are your 9 different ability cards (all are Paper)
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

    public static void registerReviveRecipe(SpcialSmp plugin) {
        // ✅ FIX: Getting the Echo Shard Revival Card
        ItemStack reviveCard = RevivalManager.getRevivalCard();

        NamespacedKey key = new NamespacedKey(plugin, "revival_card_recipe");

        ShapedRecipe recipe = new ShapedRecipe(key, reviveCard);
        
        /* Recipe Pattern:
           D E D  (D = Diamond Block, E = Echo Shard)
           T N T  (T = Totem, N = Nether Star)
           D B D  (D = Diamond Block, B = Beacon)
        */
        recipe.shape("DED", "TNT", "DBD");

        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('E', Material.ECHO_SHARD);
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('N', Material.NETHER_STAR);
        recipe.setIngredient('B', Material.BEACON);

        Bukkit.addRecipe(recipe);
    }
}
