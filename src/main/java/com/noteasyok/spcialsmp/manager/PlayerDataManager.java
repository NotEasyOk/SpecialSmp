package com.noteasyok.spcialsmp.manager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataManager {

    private final File file;
    private final YamlConfiguration cfg;

    public PlayerDataManager(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public boolean gotFirstCard(UUID uuid) {
        return cfg.getBoolean("players." + uuid + ".first", false);
    }

    public void setFirstCard(UUID uuid) {
        cfg.set("players." + uuid + ".first", true);
        save();
    }

    public boolean unlimitedCrafted(UUID uuid) {
        return cfg.getBoolean("players." + uuid + ".unlimited", false);
    }

    public void setUnlimited(UUID uuid) {
        cfg.set("players." + uuid + ".unlimited", true);
        save();
    }

    private void save() {
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
