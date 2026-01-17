package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.cards.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class CardRegistry {

    // ✅ Map wapas laya gaya hai taaki Commands mein keySet() aur containsKey() kaam kare
    private static final Map<String, BaseCard> CARDS = new LinkedHashMap<>();
    private static final Map<String, List<String>> DESC = new HashMap<>();

    public static void registerAll() {
        register(new EndermanCard(), List.of("§7Left: Teleport", "§7Right: Random TP + Invis", "§7Shift+R: Dragon Ball"));
        register(new HerobrineCard(), List.of("§7Left: Lightning", "§7Right: Darkness + Fly", "§7Shift+R: Giant/Tiny Mode"));
        register(new NothingCard(), List.of("§7Left: Time Set", "§7Right: Mind Control", "§7Shift+R: No Fall"));
        register(new ZombieCard(), List.of("§7Left: Summon Zombie", "§7Right: Feed", "§7Shift+R: Horde Mode"));
        register(new WardenCard(), List.of("§7Left: Darkness", "§7Right: Sonic Boom", "§7Shift+R: Tank Mode"));
        register(new CreeperCard(), List.of("§7Left: Explosion", "§7Right: Airstrike", "§7Shift+R: Nuke Rain"));
        register(new LightingCard(), List.of("§7Left: Strike", "§7Right: Storm", "§7Shift+R: Trail"));
        register(new GhostCard(), List.of("§7Left: Wall Clip", "§7Right: Fly", "§7Shift+R: Invisibility"));
        register(new RuinCard(), List.of("§7Left: Infection", "§7Right: Silverfish", "§7Shift+R: Poison Area"));
        
        // Ultimate card register (Spin se bahar rahega)
        register(new UltimateCard(), List.of("§6§lGOD MODE", "§eCraft Only"));
    }

    private static void register(BaseCard card, List<String> description) {
        CARDS.put(card.getName(), card);
        DESC.put(card.getName(), description);
    }

    // ✅ Yeh method CardsCommand aur SpcialSmp ki incompatibility fix karega
    public static Map<String, BaseCard> getCards() {
        return CARDS;
    }

    // ✅ Yeh method Spinner aur JoinListener ke liye zaroori hai
    public static List<String> getDescriptionLore(String key) {
        return DESC.getOrDefault(key, new ArrayList<>(List.of("§7No description available.")));
    }

    // ✅ Get Random Card Logic (Ultimate Card excluded)
    public static ItemStack getRandomCard() {
        if (CARDS.isEmpty()) return null;

        List<BaseCard> pool = CARDS.values().stream()
                .filter(c -> !c.getName().equalsIgnoreCase("Ultimate Card"))
                .collect(Collectors.toList());

        if (pool.isEmpty()) return null;

        BaseCard randomCard = pool.get(new Random().nextInt(pool.size()));

        // Item create karke lore apply karna
        ItemStack item = randomCard.createItem(); 
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            lore.add("§8------------------");
            List<String> descLines = DESC.get(randomCard.getName());
            if (descLines != null) {
                lore.addAll(descLines);
            }
            lore.add("§8------------------");
            lore.add("§e§lSPECIAL CARD");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }
                 }
