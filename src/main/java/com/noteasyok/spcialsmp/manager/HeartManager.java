package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.UUID;

public class HeartManager {

    private static final HashMap<UUID, BossBar> heartBars = new HashMap<>();

    public static void updateActionBar(Player p) {
        if (!SpcialSmp.get().getConfig().getBoolean("life-system.enabled")) {
            removeBar(p);
            return;
        }

        int lives = SpcialSmp.get().getPlayerDataManager().getLives(p.getUniqueId());
        int maxLives = SpcialSmp.get().getConfig().getInt("life-system.max-lives", 10);

        // NOTE: Ye symbols aapke texture pack ke hisaab se Hunger bar ke upar align honge
        // Agar aapke paas texture pack hai, toh unke codes yahan dalein (e.g., "\uE001")
        String heartFull = "§c❤"; 
        String heartEmpty = "§8❤";

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= maxLives; i++) {
            sb.append(i <= lives ? heartFull : heartEmpty).append(" ");
        }

        BossBar bar = heartBars.get(p.getUniqueId());
        if (bar == null) {
            // BossBar Hunger bar ke upar natural jagah banata hai
            bar = Bukkit.createBossBar(sb.toString(), BarColor.WHITE, BarStyle.SOLID);
            bar.addPlayer(p);
            heartBars.put(p.getUniqueId(), bar);
        }

        bar.setTitle(sb.toString().trim());
        bar.setVisible(true);
    }

    public static void removeBar(Player p) {
        if (heartBars.containsKey(p.getUniqueId())) {
            heartBars.get(p.getUniqueId()).removeAll();
            heartBars.remove(p.getUniqueId());
        }
    }
                }
