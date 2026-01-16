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

    /* ================= JOIN TRACKING ================= */

    public boolean hasJoinedBefore(UUID uuid) {
        return data.getBoolean("players." + uuid + ".joined", false);
    }

    public void setJoinedBefore(UUID uuid, boolean value) {
        data.set("players." + uuid + ".joined", value);
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

    /* ================= ULIIMATE CARD ================= */

    public boolean hasUltimate(UUID uuid) {
        return data.getBoolean("players." + uuid + ".ultimateCrafted", false);
    }

    public void setUltimate(UUID uuid) {
        data.set("players." + uuid + ".ultimateCrafted", true);
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
