package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataManager {

    private final File file;
    private final YamlConfiguration data;

    public PlayerDataManager(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "playerdata.yml");

        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    /* ================= FIRST JOIN CARD ================= */

    public boolean hasReceivedFirstCard(UUID uuid) {
        return data.getBoolean("players." + uuid + ".firstCard", false);
    }

    public void setReceivedFirstCard(UUID uuid, String cardName) {
        data.set("players." + uuid + ".firstCard", true);
        data.set("players." + uuid + ".firstCardName", cardName);
        saveAsync();
    }

    /* ================= ULTIMATE CARD ================= */

    public boolean hasUltimate(UUID uuid) {
        return data.getBoolean("players." + uuid + ".ultimateCrafted", false);
    }

    public void setUltimate(UUID uuid) {
        data.set("players." + uuid + ".ultimateCrafted", true);
        saveAsync();
    }

    /* ================= SAVE (PERFORMANCE FIX) ================= */

    private void saveAsync() {
        // Background thread taaki server lag na kare (Async save)
        Bukkit.getScheduler().runTaskAsynchronously(SpcialSmp.get(), this::save);
    }

    private synchronized void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }
