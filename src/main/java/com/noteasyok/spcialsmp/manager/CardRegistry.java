package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.cards.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CardRegistry {

    private static final Map<String, Card> CARDS = new LinkedHashMap<>();
    private static final Map<String, List<String>> DESCRIPTIONS = new HashMap<>();

    public static void registerAll() {
        register(new EndermanCard(), List.of(
                "Left: Teleport to aim",
                "Right: Teleport near random player + 10s invis",
                "Shift+Right (or Shift+Left): Summon 3 loyal Endermen (10s)"
        ));
        register(new HerobrineCard(), List.of(
                "Left: 5s continuous lightning",
                "Right: Darkness 10s + fly 10s",
                "Shift+Right: Strength 4 + Regen + 30 hearts + lightning"
        ));
        register(new NothingCard(), List.of(
                "Left: Toggle day/night fast",
                "Right: (Nothing)",
                "Shift+Left: No fall damage (via Slow Falling) 20s"
        ));
        register(new ZombieCard(), List.of(
                "Left: Summon baby netherite zombie 10s",
                "Shift+Right: Morph into zombie 20s"
        ));
        register(new WardenCard(), List.of(
                "Left: Darkness nearby enemies",
                "Right: Sonic boom + 50 health buff 15s",
                "Shift+Right: Magnet power for 10s"
        ));
        register(new CreeperCard(), List.of(
                "Left: Explosion",
                "Right: Orbital TNT strike (big)",
                "Shift+Right: TNT rain 5s"
        ));
        register(new LightingCard(), List.of(
                "Left: Summon lightning",
                "Right: Toggle rain",
                "Shift+Right: Lightning trail on target (no damage)"
        ));
        register(new GhostCard(), List.of(
                "Left: Pass through blocks 10s",
                "Right: Fly 40s",
                "Shift+Right: Invisible + armor 60s"
        ));
        register(new RuinCard(), List.of(
                "Left: Create infected area",
                "Right: Summon 10 Silverfish",
                "Shift+Right: Poison nearby enemies 10s"
        ));
        register(new UnlimitedCard(), List.of(
                "Unlimited powers: uses all cards' powers",
                "Craft-only item (once per player if configured)"
        ));
    }

    private static void register(Card card, List<String> description) {
        String key = card.getName() + " Card";
        CARDS.put(key, card);
        DESCRIPTIONS.put(key, description);
    }

    public static Map<String, Card> getCards() {
        return Collections.unmodifiableMap(CARDS);
    }

    public static List<ItemStack> getFirstJoinItems() {
        List<ItemStack> list = new ArrayList<>();
        for (String name : CARDS.keySet()) {
            if (name.equalsIgnoreCase("Unlimited Card")) continue;
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(DESCRIPTIONS.getOrDefault(name, List.of("No description")));
            item.setItemMeta(meta);
            list.add(item);
        }
        return list;
    }

    public static List<String> getDescriptionLore(String fullName) {
        return DESCRIPTIONS.getOrDefault(fullName, List.of("No description"));
    }
}
