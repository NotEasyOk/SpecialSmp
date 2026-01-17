package com.noteasyok.spcialsmp;

import com.noteasyok.spcialsmp.cards.BaseCard; // Added this import
import com.noteasyok.spcialsmp.command.CardsCommand;
import com.noteasyok.spcialsmp.listener.*;
import com.noteasyok.spcialsmp.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
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

        // Register cards
        CardRegistry.registerAll();

        // Recipes
        RecipeManager.registerUltimateRecipe(this);

        // ✅ FIX: Explicitly cast to Map<String, BaseCard> to avoid compilation error
        Map<String, BaseCard> cardsMap = CardRegistry.getCards();

        // Listeners
        Bukkit.getPluginManager().registerEvents(
              new CardUseListener(cardsMap), this
        );  
        Bukkit.getPluginManager().registerEvents(
              new ZombieOwnerListener(), this
        );    
        Bukkit.getPluginManager().registerEvents(
              new UltimateHoldListener(),this    
        );
        Bukkit.getPluginManager().registerEvents(
              new JoinListener(), this
        );
        Bukkit.getPluginManager().registerEvents(
              new DeathListener(), this
        );
        Bukkit.getPluginManager().registerEvents(
              new UltimateCraftListener(), this
        );
        
        // Spin GUI Protection
        Bukkit.getPluginManager().registerEvents(
              new InventoryListener(), this
        );

        // Command
        if (getCommand("cards") != null) {
            getCommand("cards").setExecutor(new CardsCommand());
        }

        getLogger().info("SpcialSmp plugin ENABLED successfully");
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
