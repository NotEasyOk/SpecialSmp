package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.*;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class CardRegistry {

    private static final Map<String, BaseCard> CARDS = new LinkedHashMap<>();
    private static final Map<String, List<String>> DESC = new HashMap<>();

    public static void registerAll() {
        CARDS.clear();
        DESC.clear();

        register(new EndermanCard(), List.of("§7Left: Teleport", "§7Right: Player Pull", "§7Shift+R: Dragon Breath"));
        register(new ZombieCard(), List.of("§7Left: Summon Zombie", "§7Right: Instant Feed", "§7Shift+R: Zombie Disguise"));
        register(new HerobrineCard(), List.of("§7Left: Lightning", "§7Right: Darkness + Fly", "§7Shift+R: Giant/Tiny Mode"));
        register(new NothingCard(), List.of("§7Left: Time Set", "§7Right: Mind Control", "§7Shift+R: No Fall damage"));
        register(new WardenCard(), List.of("§7Left: Darkness", "§7Right: Sonic Boom", "§7Shift+R: Tank Mode"));
        register(new IllusionistCard(), List.of("§7Left: Tornado", "§7Right: Info", "§7Shift+R: 5 Shadow Clones"));
        register(new RealitySeverCard(), List.of("§b§lMASTERPIECE", "§7Left: Dimension Slash", "§7Right: Gravity Crush", "§7Shift+R: Orbital Strike"));
        register(new ArchitectCard(), List.of("§7Left: Bedrock Prison", "§7Right: Kinetic Barrier", "§7Shift+R: Sky Gold Bridge"));
        register(new MirrorCard(), List.of("§7Left: Identity Thief", "§7Right: Reality Glitch", "§7Shift+R: Damage Reflection"));
        register(new GravityCard(), List.of("§7Left: Gravity Yeet", "§7Right: Black Hole Vortex", "§7Shift+R: Zero Gravity Wall"));
        register(new CreeperCard(), List.of("§7Left: Explosion", "§7Right: Airstrike", "§7Shift+R: Nuke Rain"));
        register(new LightingCard(), List.of("§7Left: Strike", "§7Right: Storm", "§7Shift+R: Trail"));
        register(new GhostCard(), List.of("§7Left: Wall Clip", "§7Right: Fly", "§7Shift+R: Invisibility"));
        register(new RuinCard(), List.of("§7Left: Ruin Dimension", "§7Right: Dark Shield", "§7Shift+R: Sliverfish Army"));
        register(new PoseidonCard(), List.of("§7Left: Tidal Beam", "§7Right: Water Prison", "§7Shift: Ocean Wrath"));
        register(new UltimateCard(), List.of("§6§lGOD MODE", "§eOnly available via special craft", "§bUnstoppable Power"));
    }

    // ================== FIXED ERROR: ADDED MISSING METHOD ==================
    public static List<String> getDescriptionLore(String cardName) {
        return DESC.getOrDefault(cardName, new ArrayList<>());
    }
    // =======================================================================

    private static void register(BaseCard card, List<String> description) {
        CARDS.put(card.getName(), card);
        DESC.put(card.getName(), description);
    }

    public static Map<String, BaseCard> getCards() {
        return CARDS;
    }

    public static ItemStack getRandomCard() {
        if (CARDS.isEmpty()) return null;

        List<BaseCard> pool = CARDS.values().stream()
                .filter(c -> !c.getName().equalsIgnoreCase("Ultimate Card"))
                .collect(Collectors.toList());

        if (pool.isEmpty()) return null;

        BaseCard randomCard = pool.get(new Random().nextInt(pool.size()));
        return getCardItem(randomCard);
    }

    public static ItemStack getCardItem(BaseCard card) {
        // Step 1: Pehle card ka base item lo (Isme PDC 'card_id' already hai)
        ItemStack item = card.getItemStackWithLore(card.getName()); 
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Step 2: Lore ko update karo bina PDC ko touch kiye
            List<String> lore = new ArrayList<>();
            lore.add("§8------------------");
            List<String> descLines = DESC.get(card.getName());
            if (descLines != null) lore.addAll(descLines);
            lore.add("§8------------------");
            lore.add("§e§lSPECIAL CARD");
            
            meta.setLore(lore);
            
            // Step 3: Meta wapas set karo (Isse lore aur PDC dono save rahenge)
            item.setItemMeta(meta);
        }
        return item;
            }
        }
