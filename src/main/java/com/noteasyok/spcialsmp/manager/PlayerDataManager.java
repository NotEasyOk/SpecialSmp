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
    
   /* ================= FUEL SYSTEM (FIXED & SYNCED) ================= */

    // FuelManager int use kar raha hai, isliye yahan bhi int rakha hai
    public int getFuel(UUID uuid) {
        return data.getInt("players." + uuid + ".fuel", -1);
    }

    public void setFuel(UUID uuid, int amount) {
        int finalAmount = Math.max(0, amount);
        data.set("players." + uuid + ".fuel", finalAmount);
        
        // ZAROORI: Fuel ke liye turant save karo bina delay ke
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ================= JOIN TRACKING ================= */

    public boolean hasJoinedBefore(UUID uuid) {
        return data.getBoolean("players." + uuid + ".joined", false);
    }

    public void setJoinedBefore(UUID uuid, boolean value) {
        data.set("players." + uuid + ".joined", value);
        saveAsync();
    }

    public long getLastBookTime(UUID uuid) {
        return data.getLong("players." + uuid + ".lastBookTime", 0L);
    }

    public void setLastBookTime(UUID uuid, long time) {
        data.set("players." + uuid + ".lastBookTime", time);
        saveAsync();
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

    /* ================= LOGOUT & OFFLINE SYSTEM ================= */

    public long getLastLogout(UUID uuid) {
        return data.getLong("players." + uuid + ".lastLogout", 0L);
    }

    public void setLastLogout(UUID uuid, long timestamp) {
        data.set("players." + uuid + ".lastLogout", timestamp);
        saveAsync();
    }

    /* ================= SAVE (PERFORMANCE FIX) ================= */

    private void saveAsync() {
        // File saving ko background thread par shift kiya taaki TPS drop na ho
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
