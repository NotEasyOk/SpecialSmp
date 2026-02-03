package com.noteasyok.spcialsmp;

import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.cards.RuinWorldGenerator;
import com.noteasyok.spcialsmp.command.CardsCommand;
import com.noteasyok.spcialsmp.listener.*; // Saare listeners yahan se aayenge
import com.noteasyok.spcialsmp.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.generator.ChunkGenerator;
import java.util.Map;

public class SpcialSmp extends JavaPlugin {

    private static SpcialSmp instance;

    private CooldownManager cooldownManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // Managers
        cooldownManager = new CooldownManager(this);
        playerDataManager = new PlayerDataManager(this);

        // 1. Sabse pehle Cards register karo
        CardRegistry.registerAll();

        com.noteasyok.spcialsmp.cards.RuinCard.preLoadDimension();

        // 2. Ab saari Recipes ek saath register karo
        RecipeManager.registerAllRecipes(this);

        // Fuel System & Task Timer Start
        FuelManager.startFuelTask();
        TaskManager.startGlobalTaskTimer();

        // Map for listeners
        Map<String, BaseCard> cardsMap = CardRegistry.getCards();

        // Custom Events for Cards (Ultimate, Ruin, etc.)
        cardsMap.values().forEach(card -> {
            if (card instanceof org.bukkit.event.Listener) {
                Bukkit.getPluginManager().registerEvents((org.bukkit.event.Listener) card, this);
            }
        });

        // --- Listeners Registration ---
        Bukkit.getPluginManager().registerEvents(new CardUseListener(cardsMap), this);  
        Bukkit.getPluginManager().registerEvents(new ZombieOwnerListener(), this);    
        Bukkit.getPluginManager().registerEvents(new UltimateHoldListener(), this);    
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new UltimateCraftListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
        Bukkit.getPluginManager().registerEvents(new TaskCompletionListener(), this);
        Bukkit.getPluginManager().registerEvents(new RevivalListener(), this);
        
        // RUIN DIMENSION EFFECTS REGISTER (Naya Add Kiya)
        Bukkit.getPluginManager().registerEvents(new RuinWorldListener(), this);

        // Command
        if (getCommand("cards") != null) {
            getCommand("cards").setExecutor(new CardsCommand());
        }

        getLogger().info("SpcialSmp plugin ENABLED successfully with Fuel, Task & Revival System");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (worldName.equals("world_ruin_dimension")) {
            return new RuinWorldGenerator();
        }
        return null;
    }

    @Override
    public void onDisable() {
        getLogger().info("SpcialSmp plugin DISABLED");
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
