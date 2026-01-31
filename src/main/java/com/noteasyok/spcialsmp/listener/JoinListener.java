package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardSpinner;
import com.noteasyok.spcialsmp.manager.FuelManager;
import com.noteasyok.spcialsmp.manager.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey; // Added
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType; // Added

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        var dataManager = SpcialSmp.get().getPlayerDataManager();

        // 1. Fuel Logic
        if (!p.hasPlayedBefore()) {
            FuelManager.setFuel(p, 1440);
        }

        // 2. Task Logic (FIXED: Ab duplicate book nahi milegi agar chest mein hai)
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            if (!p.isOnline()) return;

            // Date Check Logic: Check karo aaj task mila hai ya nahi
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "daily_task_day");
            long currentDay = System.currentTimeMillis() / 86400000L; // 1 Day in millis
            Long storedDay = p.getPersistentDataContainer().get(key, PersistentDataType.LONG);

            // Agar aaj task nahi mila (storedDay != currentDay) TABHI naya task do
            if (!hasTaskBook(p) && (storedDay == null || storedDay != currentDay)) {
                
                TaskManager.giveRandomTask(p);
                
                // Save kar lo ki aaj ka task mil gaya
                p.getPersistentDataContainer().set(key, PersistentDataType.LONG, currentDay);

                p.sendMessage("§6§lSURVIVAL BOT §8» §fA new task has been assigned! Complete it to survive.");
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
        }, 100L); 

        // 3. Reset Scale
        if (p.getAttribute(Attribute.GENERIC_SCALE) != null) 
            p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);

        // 4. First Time Card Spin
        if (!dataManager.hasReceivedFirstCard(p.getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
                 if (p.isOnline()) CardSpinner.openSpinGUI(p);
            }, 140L);
        }
    }

    // Task check logic (Same as before)
    private boolean hasTaskBook(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Aaj ka task:")) {
                    return true;
                }
            }
        }
        return false;
    }
                }
