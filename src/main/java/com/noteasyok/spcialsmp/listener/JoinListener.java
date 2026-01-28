package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardSpinner;
import com.noteasyok.spcialsmp.manager.FuelManager;
import com.noteasyok.spcialsmp.manager.TaskManager; // Naya Import
import org.bukkit.Bukkit; // Naya Import
import org.bukkit.Sound; // Naya Import
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        var dataManager = SpcialSmp.get().getPlayerDataManager();

        // --- FUEL SYSTEM START ---
        if (!p.hasPlayedBefore()) {
            FuelManager.setFuel(p, 1440);
        }
        // --- FUEL SYSTEM END ---

        // --- TASK BOOK SYSTEM (ADDED) ---
        // Testing ke liye 10 Second (200 Ticks) rakha hai.
        // Agar 1 Ghanta chahiye to 200L ko hata kar 72000L kar dena.
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            if (p.isOnline()) {
                TaskManager.giveRandomTask(p);
                p.sendMessage("§6§l[BOT] §eYe lo aaj ka Task Book!");
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
        }, 200L); 
        // --------------------------------

        if (p.getAttribute(Attribute.GENERIC_SCALE) != null) 
            p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);

        if (!dataManager.hasReceivedFirstCard(p.getUniqueId())) {
            // Thoda delay taaki crash na ho
            Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
                 CardSpinner.openSpinGUI(p);
            }, 40L);
        }
    }
}
