package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.cards.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CardRegistry {

    private static final Map<String, Card> CARDS = new HashMap<>();

    public static void registerAll() {
        register(new EndermanCard());
        register(new HerobrineCard());
        register(new NothingCard());
        register(new ZombieCard());
        register(new WardenCard());
        register(new CreeperCard());
        register(new LightingCard());
        register(new GhostCard());
        register(new RuinCard());
        register(new UnlimitedCard());
    }

    private static void register(Card card) {
        CARDS.put(card.getName() + " Card", card);
    }

    public static Map<String, Card> getCards() {
        return CARDS;
    }

    // 🔥 FIRST JOIN ITEMS (Unlimited excluded)
    public static List<ItemStack> getFirstJoinItems() {
        List<ItemStack> list = new ArrayList<>();

        for (String name : CARDS.keySet()) {
            if (name.equalsIgnoreCase("Unlimited Card")) continue;

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            item.setItemMeta(meta);
            list.add(item);
        }
        return list;
    }
}
