package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HeartManager {

    public static void startHeartDisplayTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Config check: Agar system off hai toh display mat dikhao
                if (!SpcialSmp.get().getConfig().getBoolean("life-system.enabled")) return;

                for (Player p : Bukkit.getOnlinePlayers()) {
                    updateActionBar(p);
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L); // 1 Second refresh
    }

    public static void updateActionBar(Player p) {
        FileConfiguration config = SpcialSmp.get().getConfig();
        int lives = SpcialSmp.get().getPlayerDataManager().getLives(p.getUniqueId());
        int maxLives = config.getInt("life-system.max-lives", 5);

        String iconAlive = config.getString("life-system.icons.alive", "§c❤");
        String iconEmpty = config.getString("life-system.icons.empty", "§7♡");

        StringBuilder bar = new StringBuilder();

        // Texture Logic:
        // Agar 3 lives hain aur max 5 hai:  ❤ ❤ ❤ ♡ ♡
        for (int i = 1; i <= maxLives; i++) {
            if (i <= lives) {
                bar.append(iconAlive).append(" ");
            } else {
                bar.append(iconEmpty).append(" ");
            }
        }

        // Action Bar bhejho (Hunger bar ke theek upar)
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(bar.toString()));
    }
        }
