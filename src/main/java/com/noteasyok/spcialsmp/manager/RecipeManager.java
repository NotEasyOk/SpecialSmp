package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import java.lang.CloneNotSupportedException;
import org.bukkit.inventory.RecipeChoice;
import java.util.Map;

public class RecipeManager {

    public static void registerAllRecipes(SpcialSmp plugin) {
        if (CardRegistry.getCards().isEmpty()) {
            Bukkit.getLogger().severe("[SpcialSmp] Recipes skip ho rahi hain kyunki CardRegistry khali hai!");
            return;
        }
        registerUltimateRecipe(plugin);
        // registerReviveRecipe(plugin); // LINE 19: Isko hata diya kyunki Life system nahi hai
    }

    static class CardChoice implements RecipeChoice {
    private final String cardName;
    private final Material fallbackMaterial;

    public CardChoice(String cardName, Material fallbackMaterial) {
        this.cardName = cardName;
        this.fallbackMaterial = fallbackMaterial;
    }

    @Override
    public boolean test(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (item.getType() != fallbackMaterial) return false;
        
        if (!item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        String cardId = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return cardName.equals(cardId);
    }
        
        @Override
    public RecipeChoice clone() {
        try {
            return (RecipeChoice) super.clone();
        } catch (CloneNotSupportedException e) {
            return new CardChoice(cardName, fallbackMaterial);
        }
    }
    }
    
    public static void registerUltimateRecipe(SpcialSmp plugin) {
        Map<String, BaseCard> cards = CardRegistry.getCards();
        
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

    private static RecipeChoice getCardChoice(Map<String, BaseCard> cards, String name, Material fallback) {
        BaseCard card = cards.get(name);
        if (card != null) {
            return new CardChoice(name, fallback);
        }
        return new RecipeChoice.MaterialChoice(fallback);
    }
} // LINE 58: Class sahi se band ho gayi
