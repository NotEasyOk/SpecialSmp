package com.noteasyok.spcialsmp.manager;

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

   /* ================= FUEL SYSTEM (FIXED FOR WITHDRAW/ADD) ================= */

    public long getFuel(UUID uuid) {
        // long use karein taaki bade numbers (86400+) error na dein
        return data.getLong("players." + uuid + ".fuel", -1L);
    }

    public void setFuel(UUID uuid, long amount) {
        // Value ko hamesha zero se upar rakhein
        long finalAmount = Math.max(0, amount);
        data.set("players." + uuid + ".fuel", finalAmount);
        save();
    }

    /* ================= JOIN TRACKING ================= */

    public boolean hasJoinedBefore(UUID uuid) {
        return data.getBoolean("players." + uuid + ".joined", false);
    }

    public void setJoinedBefore(UUID uuid, boolean value) {
        data.set("players." + uuid + ".joined", value);
        save();
    }

    public long getLastBookTime(UUID uuid) {
    return data.getLong("players." + uuid + ".lastBookTime", 0L);
}

public void setLastBookTime(UUID uuid, long time) {
    data.set("players." + uuid + ".lastBookTime", time);
    save();
}

    /* ================= FIRST JOIN CARD ================= */

    public boolean hasReceivedFirstCard(UUID uuid) {
        return data.getBoolean("players." + uuid + ".firstCard", false);
    }

    public void setReceivedFirstCard(UUID uuid, String cardName) {
        data.set("players." + uuid + ".firstCard", true);
        data.set("players." + uuid + ".firstCardName", cardName);
        save();
    }

    /* ================= ULTIMATE CARD ================= */

    public boolean hasUltimate(UUID uuid) {
        return data.getBoolean("players." + uuid + ".ultimateCrafted", false);
    }

    public void setUltimate(UUID uuid) {
        data.set("players." + uuid + ".ultimateCrafted", true);
        save();
    }

    /* ================= YE WALA CODE PASTE KAREIN (FIXED) ================= */

    // Last logout time nikalne ke liye (Offline fuel drain fix)
    public long getLastLogout(UUID uuid) {
        return data.getLong("players." + uuid + ".lastLogout", 0L);
    }

    // Last logout time save karne ke liye
    public void setLastLogout(UUID uuid, long timestamp) {
        data.set("players." + uuid + ".lastLogout", timestamp);
        save();
    }

    /* ================= SAVE ================= */

    private void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
