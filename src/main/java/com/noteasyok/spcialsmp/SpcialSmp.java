package com.noteasyok.spcialsmp;

import com.noteasyok.spcialsmp.command.CardsCommand;
import com.noteasyok.spcialsmp.listener.*;
import com.noteasyok.spcialsmp.manager.*;
import com.noteasyok.spicialsmp.listener.ZombieOwnerListener
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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
        RecipeManager.registerUnlimitedRecipe();

        // Listeners
        Bukkit.getPluginManager().registerEvents(
                new CardUseListener(CardRegistry.getCards()), this
        );
        Bukkit.getPluginManager().registerEvents(
                new CardInventoryInfoListener(), this
        );  
         Bukkit.getPluginManager().registerEvents(
             new ZombieOwnerListener(), this
        );
        Bukkit.getPluginManager().registerEvents(
                new JoinListener(CardRegistry.getFirstJoinItems()), this
        );
        Bukkit.getPluginManager().registerEvents(
                new DeathListener(), this
        );
        Bukkit.getPluginManager().registerEvents(
                new UnlimitedCraftListener(), this
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
