package com.noteasyok.spcialsmp;

import com.noteasyok.spcialsmp.listener.*;
import com.noteasyok.spcialsmp.manager.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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

        // Register Cards
        CardRegistry.registerAll();

        // Listeners
        getServer().getPluginManager().registerEvents(
                new CardUseListener(CardRegistry.getCards()), this);

        getServer().getPluginManager().registerEvents(
                new JoinListener(getFirstJoinCards()), this);

        getServer().getPluginManager().registerEvents(
                new DeathListener(), this);

        getServer().getPluginManager().registerEvents(
                new UnlimitedCraftListener(), this);

        // Commands
        getCommand("cards").setExecutor(new CardsCommand());

        getLogger().info("spcialSmp ENABLED successfully");
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

    // ❌ Unlimited Card yahan nahi hota
    private List<String> getFirstJoinCards() {
        return List.of(
                "Enderman Card",
                "Herobrine Card",
                "Nothing Card",
                "Zombie Card",
                "Warden Card",
                "Creeper Card",
                "Lighting Card",
                "Ghost Card",
                "Ruin Card"
        );
    }
}
