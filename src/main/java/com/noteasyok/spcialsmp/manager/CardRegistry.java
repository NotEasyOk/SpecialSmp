package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.cards.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CardRegistry {

    private static final Map<String, Card> CARDS = new LinkedHashMap<>();
    private static final Map<String, List<String>> DESC = new HashMap<>();

    public static void registerAll() {
        register(new EndermanCard(), List.of(
                "Left: Teleport where you look",
                "Right: Teleport near random player + invis 10s",
                "Shift+Right: Summon 3 loyal Endermen (10s)"
        ));
        register(new HerobrineCard(), List.of(
                "Left: Lightning non-stop 5s",
                "Right: Darkness 10s + fly 10s",
                "Shift+Right: Strength 4 + Reg 4 + +30 hearts (temp) + lightning 5s"
        ));
        register(new NothingCard(), List.of(
                "Left: Toggle day/night",
                "Right: Nothing",
                "Shift+Right: No fall damage 20s"
        ));
        register(new ZombieCard(), List.of(
                "Left: Summon baby netherite zombie (max per window)",
                "Right: ---",
                "Shift+Right: ---"
        ));
        register(new WardenCard(), List.of(
                "Left: Darkness nearby enemies",
                "Right: Sonic boom + big health boost (temp)",
                "Shift+Right: Magnet pull items"
        ));
        register(new CreeperCard(), List.of(
                "Left: Big explosion (5× TNT equivalent)",
                "Right: Orbital strike where you aim",
                "Shift+Right: TNT rain"
        ));
        register(new LightingCard(), List.of(
                "Left: Lightning at aim",
                "Right: Toggle rain",
                "Shift+Right: Lightning trail (no damage)"
        ));
        register(new GhostCard(), List.of(
                "Left: Pass-through blocks (simulated)",
                "Right: Fly 40s",
                "Shift+Right: Invisible 60s + armor"
        ));
        register(new RuinCard(), List.of(
                "Left: Infected area",
                "Right: Summon 10 Silverfish",
                "Shift+Right: Poison nearby enemies 10s"
        ));
        register(new UnlimitedCard(), List.of(
                "Craft-only unlimited powers"
        ));
    }

    private static void register(Card card, List<String> description) {
        String key = card.getName(); // make sure getName returns e.g. "Enderman Card"
        CARDS.put(key, card);
        DESC.put(key, description);
    }

    public static Map<String, Card> getCards() {
        return Collections.unmodifiableMap(CARDS);
    }

    public static List<String> getDescriptionLore(String key) {
        return DESC.getOrDefault(key, List.of("No description"));
    }
}
