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

    /* ================= FIRST JOIN CARD ================= */

    public boolean hasReceivedFirstCard(UUID uuid) {
        return data.getBoolean("players." + uuid + ".firstCard", false);
    }

    public void setReceivedFirstCard(UUID uuid, String cardName) {
        data.set("players." + uuid + ".firstCard", true);
        data.set("players." + uuid + ".firstCardName", cardName);
        save();
    }

    /* ================= UNLIMITED CARD ================= */

    public boolean hasUnlimited(UUID uuid) {
        return data.getBoolean("players." + uuid + ".unlimitedCrafted", false);
    }

    public void setUnlimited(UUID uuid) {
        data.set("players." + uuid + ".unlimitedCrafted", true);
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
