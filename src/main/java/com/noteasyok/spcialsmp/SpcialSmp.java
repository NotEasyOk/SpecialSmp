package com.noteasyok.spcialsmp;

import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.cards.RuinWorldGenerator;
import com.noteasyok.spcialsmp.command.CardsCommand;
import com.noteasyok.spcialsmp.command.SmpCommand;
import com.noteasyok.spcialsmp.command.LifeCommand;
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

        // --- 1. CONFIG INITIALIZATION ---
        saveDefaultConfig();
        reloadConfig(); 

        // --- 2. MANAGERS INITIALIZATION ---
        playerDataManager = new PlayerDataManager(this);
        cooldownManager = new CooldownManager(this);
        startManager = new StartManager(this);

        // --- 3. LIFE SYSTEM INITIALIZATION ---
        // Purana Fuel aur Task system delete kar diya gaya hai
        HeartManager.registerReviveRecipe();
        HeartManager.startHeartDisplayTask();

        // --- 4. CARDS & DIMENSIONS ---
        CardRegistry.registerAll();
        com.noteasyok.spcialsmp.cards.RuinCard.preLoadDimension();
        RecipeManager.registerAllRecipes(this);

        // --- 5. LISTENERS REGISTRATION ---
        Map<String, BaseCard> cardsMap = CardRegistry.getCards();
        
        // Registering card-specific listeners
        cardsMap.values().forEach(card -> {
            if (card instanceof org.bukkit.event.Listener) {
                Bukkit.getPluginManager().registerEvents((org.bukkit.event.Listener) card, this);
            }
        });

        // Essential Core Listeners
        Bukkit.getPluginManager().registerEvents(new CardUseListener(cardsMap), this);  
        Bukkit.getPluginManager().registerEvents(new ZombieOwnerListener(), this);    
        Bukkit.getPluginManager().registerEvents(new UltimateHoldListener(), this);    
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new UltimateCraftListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
        Bukkit.getPluginManager().registerEvents(new RuinWorldListener(), this);
        
        // Naya Life System Listener (Death, Animation aur Heart Use handle karta hai)
        Bukkit.getPluginManager().registerEvents(new LifeEvents(), this);

        // --- 6. COMMANDS ---
        if (getCommand("cards") != null) {
            getCommand("cards").setExecutor(new CardsCommand());
        }
    
        if (getCommand("smp") != null) {
            SmpCommand smpCmd = new SmpCommand(this);
            getCommand("smp").setExecutor(smpCmd);
            getCommand("smp").setTabCompleter(smpCmd);
        }

        // Registering the new Life command (/life withdraw/recipe)
        if (getCommand("life") != null) {
            getCommand("life").setExecutor(new LifeCommand());
        }

        getLogger().info("§a[SpcialSmp] Life System & Cards loaded successfully!");
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
