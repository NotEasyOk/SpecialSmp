package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class RecipeManager {

    public static void registerAllRecipes(SpcialSmp plugin) {
        registerUltimateRecipe(plugin);
        registerReviveRecipe(plugin); 
    }

    public static void registerUltimateRecipe(SpcialSmp plugin) {
        // Ultimate Card ab Green Dye material return karega
        ItemStack ultimateItem = CardRegistry.getCards().get("Ultimate Card").getItemStackWithLore("Ultimate Card");

        NamespacedKey key = new NamespacedKey(plugin, "ultimate_card_recipe");

        ShapedRecipe recipe = new ShapedRecipe(key, ultimateItem);
        recipe.shape("ABC", "DEF", "GHI");

        // ✅ FIXED: Ingredients ko naye card materials ke mutabik set kiya
        recipe.setIngredient('A', Material.DISC_FRAGMENT_5); // Creeper
        recipe.setIngredient('B', Material.CHORUS_FRUIT);     // Enderman
        recipe.setIngredient('C', Material.PURPLE_DYE);      // Herobrine
        recipe.setIngredient('D', Material.BLACK_DYE);       // Zombie
        recipe.setIngredient('E', Material.WHITE_DYE);       // Ghost
        recipe.setIngredient('F', Material.YELLOW_DYE);      // Lightning
        recipe.setIngredient('G', Material.GRAY_DYE);        // Ruin
        recipe.setIngredient('H', Material.MUSIC_DISC_5);    // Warden
        recipe.setIngredient('I', Material.RECOVERY_COMPASS); // Nothing

        Bukkit.addRecipe(recipe);
    }

    public static void registerReviveRecipe(SpcialSmp plugin) {
        ItemStack reviveCard = RevivalManager.getRevivalCard();

        NamespacedKey key = new NamespacedKey(plugin, "revival_card_recipe");

        ShapedRecipe recipe = new ShapedRecipe(key, reviveCard);
        
        recipe.shape("DED", "TNT", "DBD");

        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('E', Material.ECHO_SHARD);
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('N', Material.NETHER_STAR);
        recipe.setIngredient('B', Material.BEACON);

        Bukkit.addRecipe(recipe);
    }
    }
