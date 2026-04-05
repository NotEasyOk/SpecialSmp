package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import java.util.Map;

public class RecipeManager {

    public static void registerAllRecipes(SpcialSmp plugin) {
        if (CardRegistry.getCards().isEmpty()) {
            Bukkit.getLogger().severe("[SpcialSmp] Recipes are being skipped because CardRegistry is empty!");
            return;
        }
        registerUltimateRecipe(plugin);
    }

    public static void registerUltimateRecipe(SpcialSmp plugin) {
        Map<String, BaseCard> cards = CardRegistry.getCards();
        
        BaseCard ultimateCard = cards.get("Ultimate Card");
        if (ultimateCard == null) {
            Bukkit.getLogger().severe("[SpcialSmp] ERROR: Ultimate Card not found in registry!");
            return;
        }

        ItemStack ultimateItem = ultimateCard.getItemStackWithLore("Ultimate Card");
        NamespacedKey key = new NamespacedKey(plugin, "ultimate_card_recipe");

        if (Bukkit.getRecipe(key) != null) {
            Bukkit.removeRecipe(key);
        }

        ShapedRecipe recipe = new ShapedRecipe(key, ultimateItem);
        recipe.shape("ABC", "DEF", "GHI");

        // Only check material (card_id validation will be done in craft event)
        recipe.setIngredient('A', new RecipeChoice.MaterialChoice(Material.DISC_FRAGMENT_5));
        recipe.setIngredient('B', new RecipeChoice.MaterialChoice(Material.CHORUS_FRUIT));
        recipe.setIngredient('C', new RecipeChoice.MaterialChoice(Material.PURPLE_DYE));
        recipe.setIngredient('D', new RecipeChoice.MaterialChoice(Material.BLACK_DYE));
        recipe.setIngredient('E', new RecipeChoice.MaterialChoice(Material.WHITE_DYE));
        recipe.setIngredient('F', new RecipeChoice.MaterialChoice(Material.YELLOW_DYE));
        recipe.setIngredient('G', new RecipeChoice.MaterialChoice(Material.GRAY_DYE));
        recipe.setIngredient('H', new RecipeChoice.MaterialChoice(Material.MUSIC_DISC_5));
        recipe.setIngredient('I', new RecipeChoice.MaterialChoice(Material.PINK_DYE));

        Bukkit.addRecipe(recipe);
    }
            }
