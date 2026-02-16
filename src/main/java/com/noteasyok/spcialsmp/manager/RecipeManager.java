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
        // Zaroori: Pehle check karo ki cards load hue hain ya nahi
        if (CardRegistry.getCards().isEmpty()) {
            Bukkit.getLogger().severe("[SpcialSmp] Recipes skip ho rahi hain kyunki CardRegistry khali hai!");
            return;
        }
        registerUltimateRecipe(plugin);
        registerReviveRecipe(plugin); 
    }

    public static void registerUltimateRecipe(SpcialSmp plugin) {
        Map<String, BaseCard> cards = CardRegistry.getCards();
        
        // Safety Check: Agar Ultimate Card nahi mila toh crash mat ho
        BaseCard ultimateCard = cards.get("Ultimate Card");
        if (ultimateCard == null) {
            Bukkit.getLogger().severe("[SpcialSmp] ERROR: Ultimate Card registry mein nahi mila!");
            return;
        }

        ItemStack ultimateItem = ultimateCard.getItemStackWithLore("Ultimate Card");
        NamespacedKey key = new NamespacedKey(plugin, "ultimate_card_recipe");

        if (Bukkit.getRecipe(key) != null) {
            Bukkit.removeRecipe(key);
        }

        ShapedRecipe recipe = new ShapedRecipe(key, ultimateItem);
        recipe.shape("ABC", "DEF", "GHI");

        // Helper method use kar rahe hain taaki NullPointerException kabhi na aaye
        recipe.setIngredient('A', getCardChoice(cards, "Creeper Card", Material.DISC_FRAGMENT_5));
        recipe.setIngredient('B', getCardChoice(cards, "Enderman Card", Material.CHORUS_FRUIT));
        recipe.setIngredient('C', getCardChoice(cards, "Herobrine Card", Material.PURPLE_DYE));
        recipe.setIngredient('D', getCardChoice(cards, "Zombie Card", Material.BLACK_DYE));
        recipe.setIngredient('E', getCardChoice(cards, "Ghost Card", Material.WHITE_DYE));
        recipe.setIngredient('F', getCardChoice(cards, "Lightning Card", Material.YELLOW_DYE));
        recipe.setIngredient('G', getCardChoice(cards, "Ruin Card", Material.GRAY_DYE));
        recipe.setIngredient('H', getCardChoice(cards, "Warden Card", Material.MUSIC_DISC_5));
        recipe.setIngredient('I', getCardChoice(cards, "Nothing Card", Material.PINK_DYE));

        Bukkit.addRecipe(recipe);
    }

    // Naya Safe Helper Method
    private static RecipeChoice getCardChoice(Map<String, BaseCard> cards, String name, Material fallback) {
        BaseCard card = cards.get(name);
        if (card != null) {
            return new RecipeChoice.ExactChoice(card.getItemStackWithLore(name));
        }
        // Agar card registry mein nahi hai, toh fallback material use karo (taki crash na ho)
        return new RecipeChoice.MaterialChoice(fallback);
    }

        NamespacedKey key = new NamespacedKey(plugin, "revival_card_recipe");
        if (Bukkit.getRecipe(key) != null) Bukkit.removeRecipe(key);

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
