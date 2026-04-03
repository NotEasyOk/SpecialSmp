package com.noteasyok.spcialsmp;

import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.cards.RuinWorldGenerator;
import com.noteasyok.spcialsmp.command.CardsCommand;
import com.noteasyok.spcialsmp.command.SmpCommand;
import com.noteasyok.spcialsmp.manager.StartManager;
import com.noteasyok.spcialsmp.listener.*;
import com.noteasyok.spcialsmp.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.generator.ChunkGenerator;
import java.util.Map;

public class SpcialSmp extends JavaPlugin {

    private static SpcialSmp instance;
    private CooldownManager cooldownManager;
    private PlayerDataManager playerDataManager;
    private StartManager startManager;
    private FileConfiguration cardsConfig;
    private File cardsConfigFile;

    @Override
    public void onEnable() {
        instance = this;

        // --- 1. CONFIG INITIALIZATION ---
        saveDefaultConfig();
        loadCardsConfig();
        reloadConfig(); 

        public void loadCardsConfig() {
       cardsConfigFile = new File(getDataFolder(), "cards.yml");
     if (!cardsConfigFile.exists()) {
        saveResource("cards.yml", false);
      }
      cardsConfig = YamlConfiguration.loadConfiguration(cardsConfigFile);
   }

    public FileConfiguration getCardsConfig() {
       return cardsConfig;
  }

       public void reloadCardsConfig() {
      cardsConfig = YamlConfiguration.loadConfiguration(cardsConfigFile);
 }

        // --- 2. MANAGERS INITIALIZATION ---
        playerDataManager = new PlayerDataManager(this);
        cooldownManager = new CooldownManager(this);
        startManager = new StartManager(this);

        // --- 3. CARDS & DIMENSIONS ---
        CardRegistry.registerAll();
        com.noteasyok.spcialsmp.cards.RuinCard.preLoadDimension();
        RecipeManager.registerAllRecipes(this);

        // --- 4. LISTENERS REGISTRATION ---
        Map<String, BaseCard> cardsMap = CardRegistry.getCards();
        
        // Registering card-specific listeners
        cardsMap.values().forEach(card -> {
            if (card instanceof org.bukkit.event.Listener) {
                Bukkit.getPluginManager().registerEvents((org.bukkit.event.Listener) card, this);
            }
        });

        // Essential Core Listeners (No Life/Fuel Listeners here)
        Bukkit.getPluginManager().registerEvents(new CardUseListener(cardsMap), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new ZombieOwnerListener(), this);    
        Bukkit.getPluginManager().registerEvents(new UltimateHoldListener(), this);    
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new UltimateCraftListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
        Bukkit.getPluginManager().registerEvents(new RuinWorldListener(), this);
        
        // Note: LifeEvents and DeathListener removed to keep it purely card-based

        // --- 5. COMMANDS ---
        if (getCommand("cards") != null) {
            getCommand("cards").setExecutor(new CardsCommand());
        }
    
        if (getCommand("smp") != null) {
            SmpCommand smpCmd = new SmpCommand(this);
            getCommand("smp").setExecutor(smpCmd);
            getCommand("smp").setTabCompleter(smpCmd);
        }

        getLogger().info("§b[SpcialSmp] Special Cards System loaded! (successful)");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (worldName != null && worldName.equals("world_ruin_dimension")) {
            return new RuinWorldGenerator();
        }
        return null;
    }

    @Override
    public void onDisable() {
        instance = null;
        getLogger().info("§c[SpcialSmp] Plugin disabled.");
    }

    public static SpcialSmp get() {
        return instance;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public StartManager getStartManager() {
        return startManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
            }
