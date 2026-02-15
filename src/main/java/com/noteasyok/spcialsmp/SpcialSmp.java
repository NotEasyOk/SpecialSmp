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

    @Override
public void onEnable() {
    instance = this;

    // --- 1. CONFIG INITIALIZATION (Sahi Tarika) ---
    // Sirf ye line kafi hai. Ye error nahi degi agar file pehle se hai.
    saveDefaultConfig();
    
    // Agar tune manually config edit ki hai, toh ye usse memory mein refresh kar dega
    reloadConfig(); 

    // --- 2. MANAGERS INITIALIZATION ---
    playerDataManager = new PlayerDataManager(this);
    cooldownManager = new CooldownManager(this);
    startManager = new StartManager(this);

        // --- 3. FUEL SYSTEM CHECK ---
        // Null safety ke saath task start karein
        try {
            FuelManager.startFuelTask();
        } catch (Exception e) {
            getLogger().severe("Fuel System load nahi ho saka! Config check karein.");
        }

        // --- 4. CARDS & RECIPES ---
        CardRegistry.registerAll();
        com.noteasyok.spcialsmp.cards.RuinCard.preLoadDimension();
        RecipeManager.registerAllRecipes(this);
        TaskManager.startGlobalTaskTimer();

        // --- 5. LISTENERS REGISTRATION ---
        Map<String, BaseCard> cardsMap = CardRegistry.getCards();
        
        // Registering individual card listeners
        cardsMap.values().forEach(card -> {
            if (card instanceof org.bukkit.event.Listener) {
                Bukkit.getPluginManager().registerEvents((org.bukkit.event.Listener) card, this);
            }
        });

        // Event Listeners
        Bukkit.getPluginManager().registerEvents(new CardUseListener(cardsMap), this);  
        Bukkit.getPluginManager().registerEvents(new ZombieOwnerListener(), this);    
        Bukkit.getPluginManager().registerEvents(new UltimateHoldListener(), this);    
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new UltimateCraftListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
        Bukkit.getPluginManager().registerEvents(new TaskCompletionListener(), this);
        Bukkit.getPluginManager().registerEvents(new RevivalListener(), this);
        Bukkit.getPluginManager().registerEvents(new RuinWorldListener(), this);

        // --- 6. COMMANDS ---
        if (getCommand("cards") != null) {
            getCommand("cards").setExecutor(new CardsCommand());
            getCommand("smp").setExecutor(new SmpCommand(this));
        }

        getLogger().info("§a[SpcialSmp] Plugin loaded successfully!");
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

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
                                                  }
