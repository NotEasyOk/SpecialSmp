package com.noteasyok.spcialsmp.manager;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public enum TaskType {
    // --- MINING TASKS (30) ---
    M1("5 Diamonds mine karo", Material.DIAMOND_ORE, 5),
    M2("10 Deepslate Diamond mine karo", Material.DEEPSLATE_DIAMOND_ORE, 10),
    M3("64 Iron Ore dhoondo", Material.IRON_ORE, 64),
    M4("64 Raw Copper collect karo", Material.RAW_COPPER, 64),
    M5("32 Gold Ore mine karo", Material.GOLD_ORE, 32),
    M6("5 Emeralds mine karo", Material.EMERALD_ORE, 5),
    M7("128 Coal collect karo", Material.COAL, 128),
    M8("20 Redstone Ore mine karo", Material.REDSTONE_ORE, 20),
    M9("32 Lapis Lazuli Ore mine karo", Material.LAPIS_ORE, 32),
    M10("2 Ancient Debris mine karo", Material.ANCIENT_DEBRIS, 2),
    M11("64 Nether Quartz collect karo", Material.QUARTZ, 64),
    M12("32 Nether Gold Ore mine karo", Material.NETHER_GOLD_ORE, 32),
    M13("500 Stone blocks todo", Material.STONE, 500),
    M14("200 Deepslate todo", Material.DEEPSLATE, 200),
    M15("100 Granite todo", Material.GRANITE, 100),
    M16("100 Diorite todo", Material.DIORITE, 100),
    M17("100 Andesite todo", Material.ANDESITE, 100),
    M18("64 Amethyst Shards collect karo", Material.AMETHYST_SHARD, 64),
    M19("32 Glowstone todo", Material.GLOWSTONE, 32),
    M20("64 Obsidian mine karo", Material.OBSIDIAN, 64),
    M21("128 Gravel todo", Material.GRAVEL, 128),
    M22("256 Netherrack todo", Material.NETHERRACK, 256),
    M23("32 Soul Sand collect karo", Material.SOUL_SAND, 32),
    M24("32 Magma Blocks collect karo", Material.MAGMA_BLOCK, 32),
    M25("10 Crying Obsidian dhoondo", Material.CRYING_OBSIDIAN, 10),
    M26("64 Basalt mine karo", Material.BASALT, 64),
    M27("32 Blackstone collect karo", Material.BLACKSTONE, 32),
    M28("10 Gilded Blackstone dhoondo", Material.GILDED_BLACKSTONE, 10),
    M29("64 Calcite collect karo", Material.CALCITE, 64),
    M30("32 Tuff blocks todo", Material.TUFF, 32),

    // --- HUNTING TASKS (35) ---
    H1("25 Zombies ko maaro", EntityType.ZOMBIE, 25),
    H2("20 Skeletons ko khatam karo", EntityType.SKELETON, 20),
    H3("15 Creepers ko maaro", EntityType.CREEPER, 15),
    H4("15 Spiders ko maaro", EntityType.SPIDER, 15),
    H5("5 Enderman ko khatam karo", EntityType.ENDERMAN, 5),
    H6("20 Slimes ko maaro", EntityType.SLIME, 20),
    H7("10 Witches ko khatam karo", EntityType.WITCH, 10),
    H8("30 Drowned maaro", EntityType.DROWNED, 30),
    H9("10 Husks ko maaro", EntityType.HUSK, 10),
    H10("10 Strays ko maaro", EntityType.STRAY, 10),
    H11("5 Cave Spiders ko maaro", EntityType.CAVE_SPIDER, 5),
    H12("15 Piglins ko khatam karo", EntityType.PIGLIN, 15),
    H13("10 Hoglins ko maaro", EntityType.HOGLIN, 10),
    H14("5 Blazes ko maaro", EntityType.BLAZE, 5),
    H15("3 Ghasts ko khatam karo", EntityType.GHAST, 3),
    H16("5 Wither Skeletons maaro", EntityType.WITHER_SKELETON, 5),
    H17("20 Magma Cubes maaro", EntityType.MAGMA_CUBE, 20),
    H18("10 Zombified Piglins maaro", EntityType.ZOMBIFIED_PIGLIN, 10),
    H19("5 Guardians ko khatam karo", EntityType.GUARDIAN, 5),
    H20("20 Phantoms ko maaro", EntityType.PHANTOM, 20),
    H21("5 Pillagers ko maaro", EntityType.PILLAGER, 5),
    H22("2 Ravagers ko khatam karo", EntityType.RAVAGER, 2),
    H23("10 Vindicators maaro", EntityType.VINDICATOR, 10),
    H24("5 Evokers ko maaro", EntityType.EVOKER, 5),
    H25("15 Shulkers ko khatam karo", EntityType.SHULKER, 15),
    H26("10 Silverfish maaro", EntityType.SILVERFISH, 10),
    H27("5 Endermites maaro", EntityType.ENDERMITE, 5),
    H28("20 Bees ko maaro (if u dare!)", EntityType.BEE, 20),
    H29("10 Iron Golems ko khatam karo", EntityType.IRON_GOLEM, 10),
    H30("1 Elder Guardian maaro", EntityType.ELDER_GUARDIAN, 1),
    H31("10 Foxes ko khatam karo", EntityType.FOX, 10),
    H32("20 Glow Squids ko maaro", EntityType.GLOW_SQUID, 20),
    H33("10 Goats ko maaro", EntityType.GOAT, 10),
    H34("5 Polar Bears ko maaro", EntityType.POLAR_BEAR, 5),
    H35("10 Llamas ko maaro", EntityType.LLAMA, 10),

    // --- WOODCUTTING & FARMING (35) ---
    W1("128 Oak Logs kato", Material.OAK_LOG, 128),
    W2("128 Birch Logs kato", Material.BIRCH_LOG, 128),
    W3("128 Spruce Logs kato", Material.SPRUCE_LOG, 128),
    W4("128 Jungle Logs kato", Material.JUNGLE_LOG, 128),
    W5("128 Dark Oak Logs kato", Material.DARK_OAK_LOG, 128),
    W6("128 Acacia Logs kato", Material.ACACIA_LOG, 128),
    W7("64 Crimson Stems collect karo", Material.CRIMSON_STEM, 64),
    W8("64 Warped Stems collect karo", Material.WARPED_STEM, 64),
    W9("128 Mangrove Logs kato", Material.MANGROVE_LOG, 128),
    W10("128 Cherry Logs kato", Material.CHERRY_LOG, 128),
    F1("64 Wheat harvest karo", Material.WHEAT, 64),
    F2("64 Carrots collect karo", Material.CARROT, 64),
    F3("64 Potatoes collect karo", Material.POTATO, 64),
    F4("32 Pumpkins harvest karo", Material.PUMPKIN, 32),
    F5("32 Melons harvest karo", Material.MELON, 32),
    F6("64 Sugar Cane collect karo", Material.SUGAR_CANE, 64),
    F7("64 Bamboo collect karo", Material.BAMBOO, 64),
    F8("128 Cactus collect karo", Material.CACTUS, 128),
    F9("64 Sweet Berries collect karo", Material.SWEET_BERRIES, 64),
    F10("32 Mushrooms collect karo", Material.RED_MUSHROOM, 32),
    F11("64 Nether Wart harvest karo", Material.NETHER_WART, 64),
    F12("32 Sea Pickles collect karo", Material.SEA_PICKLE, 32),
    F13("64 Kelp collect karo", Material.KELP, 64),
    F14("32 Sea Grass collect karo", Material.SEAGRASS, 32),
    F15("10 Lily Pads dhoondo", Material.LILY_PAD, 10),
    F16("64 Vines collect karo", Material.VINE, 64),
    F17("32 Glow Berries collect karo", Material.GLOW_BERRIES, 32),
    F18("64 Moss Blocks collect karo", Material.MOSS_BLOCK, 64),
    F19("10 Spore Blossoms dhoondo", Material.SPORE_BLOSSOM, 10),
    F20("64 Azalea Leaves collect karo", Material.AZALEA_LEAVES, 64),
    F21("32 Big Dripleaf collect karo", Material.BIG_DRIPLEAF, 32),
    F22("10 Pink Petals dhoondo", Material.PINK_PETALS, 10),
    F23("64 Cocoa Beans collect karo", Material.COCOA_BEANS, 64),
    F24("32 Chorus Fruit collect karo", Material.CHORUS_FRUIT, 32),
    F25("64 Dried Kelp Blocks banao", Material.DRIED_KELP_BLOCK, 64),

    // --- CRAFTING & MISC (25) ---
    C1("64 Bread craft karo", Material.BREAD, 64),
    C2("32 Cooked Beef (Steak) banao", Material.COOKED_BEEF, 32),
    C3("32 Cooked Porkchop banao", Material.COOKED_PORKCHOP, 32),
    C4("16 Golden Apples craft karo", Material.GOLDEN_APPLE, 16),
    C5("1 Enchanted Golden Apple dhoondo", Material.ENCHANTED_GOLDEN_APPLE, 1),
    C6("64 Torches craft karo", Material.TORCH, 64),
    C7("10 Bookshelves craft karo", Material.BOOKSHELF, 10),
    C8("5 TNT blocks craft karo", Material.TNT, 5),
    C9("10 Hay Bales craft karo", Material.HAY_BLOCK, 10),
    C10("20 White Wool collect karo", Material.WHITE_WOOL, 20),
    C11("10 Glass Bottles craft karo", Material.GLASS_BOTTLE, 10),
    C12("5 Anvils craft karo", Material.ANVIL, 5),
    C13("10 Hoppers craft karo", Material.HOPPER, 10),
    C14("1 Beacon dhoondo ya banao", Material.BEACON, 1),
    C15("20 Barrels craft karo", Material.BARREL, 20),
    C16("10 Lanterns craft karo", Material.LANTERN, 10),
    C17("20 Fishing Rods craft karo", Material.FISHING_ROD, 20),
    C18("10 Pistons craft karo", Material.PISTON, 10),
    C19("5 Sticky Pistons banao", Material.STICKY_PISTON, 5),
    C20("10 Observers craft karo", Material.OBSERVER, 10),
    C21("20 Dispensers craft karo", Material.DISPENSER, 20),
    C22("32 Item Frames craft karo", Material.ITEM_FRAME, 32),
    C23("10 Armor Stands craft karo", Material.ARMOR_STAND, 10),
    C24("16 Ender Chests dhoondo ya banao", Material.ENDER_CHEST, 16),
    C25("1 Nether Star collect karo", Material.NETHER_STAR, 1);

    private final String description;
    private final Object target;
    private final int amount;

    TaskType(String description, Object target, int amount) {
        this.description = description;
        this.target = target;
        this.amount = amount;
    }

    public String getDescription() { return description; }
    public Object getTarget() { return target; }
    public int getAmount() { return amount; }

    public static TaskType getRandom() {
        return values()[(int) (Math.random() * values().length)];
    }
        }
