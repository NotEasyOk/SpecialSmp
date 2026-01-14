package com.noteasyok.spcialsmp;

import com.noteasyok.spcialsmp.command.CardsCommand;
import com.noteasyok.spcialsmp.listener.CardUseListener;
import com.noteasyok.spcialsmp.listener.DeathListener;
import com.noteasyok.spcialsmp.listener.JoinListener;
import com.noteasyok.spcialsmp.listener.UnlimitedCraftListener;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import com.noteasyok.spcialsmp.manager.CooldownManager;
import com.noteasyok.spcialsmp.manager.PlayerDataManager;
import com.noteasyok.spcialsmp.manager.RecipeManager;
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
        cooldownManager = new CooldownManager();
        playerDataManager = new PlayerDataManager(this);

        // Register all cards
        CardRegistry.registerAll();

        // Register Unlimited Card recipe
        RecipeManager.registerUnlimitedRecipe();

        // Register listeners
        Bukkit.getPluginManager().registerEvents(
                new CardUseListener(CardRegistry.getCards()), this
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

        getLogger().info("SpcialSmp plugin ENABLED");
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
 
