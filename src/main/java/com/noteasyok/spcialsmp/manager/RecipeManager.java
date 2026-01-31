package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.RecipeChoice;
import java.util.Map;

public class RecipeManager {

    public static void registerAllRecipes(SpcialSmp plugin) {
        registerUltimateRecipe(plugin);
        registerReviveRecipe(plugin); 
    }

    public static void registerUltimateRecipe(SpcialSmp plugin) {
        Map<String, BaseCard> cards = CardRegistry.getCards();
        
        // Ultimate Card result item
        ItemStack ultimateItem = cards.get("Ultimate Card").getItemStackWithLore("Ultimate Card");
        NamespacedKey key = new NamespacedKey(plugin, "ultimate_card_recipe");

        // Purani recipe remove karo agar exist karti ho (to avoid conflicts)
        if (Bukkit.getRecipe(key) != null) {
            Bukkit.removeRecipe(key);
        }

        ShapedRecipe recipe = new ShapedRecipe(key, ultimateItem);
        recipe.shape("ABC", "DEF", "GHI");

        // Har slot ke liye card ka exact ItemStack check hoga
        recipe.setIngredient('A', new RecipeChoice.ExactChoice(cards.get("Creeper Card").getItemStackWithLore("Creeper Card")));
        recipe.setIngredient('B', new RecipeChoice.ExactChoice(cards.get("Enderman Card").getItemStackWithLore("Enderman Card")));
        recipe.setIngredient('C', new RecipeChoice.ExactChoice(cards.get("Herobrine Card").getItemStackWithLore("Herobrine Card")));
        recipe.setIngredient('D', new RecipeChoice.ExactChoice(cards.get("Zombie Card").getItemStackWithLore("Zombie Card")));
        recipe.setIngredient('E', new RecipeChoice.ExactChoice(cards.get("Ghost Card").getItemStackWithLore("Ghost Card")));
        recipe.setIngredient('F', new RecipeChoice.ExactChoice(cards.get("Lightning Card").getItemStackWithLore("Lightning Card")));
        recipe.setIngredient('G', new RecipeChoice.ExactChoice(cards.get("Ruin Card").getItemStackWithLore("Ruin Card")));
        recipe.setIngredient('H', new RecipeChoice.ExactChoice(cards.get("Warden Card").getItemStackWithLore("Warden Card")));
        recipe.setIngredient('I', new RecipeChoice.ExactChoice(cards.get("Nothing Card").getItemStackWithLore("Nothing Card")));

        Bukkit.addRecipe(recipe);
    }

    public static void registerReviveRecipe(SpcialSmp plugin) {
        ItemStack reviveCard = RevivalManager.getRevivalCard();
        NamespacedKey key = new NamespacedKey(plugin, "revival_card_recipe");

        if (Bukkit.getRecipe(key) != null) {
            Bukkit.removeRecipe(key);
        }

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
