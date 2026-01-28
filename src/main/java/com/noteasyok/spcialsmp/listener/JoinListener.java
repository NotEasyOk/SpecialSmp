package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardSpinner;
import com.noteasyok.spcialsmp.manager.FuelManager;
import com.noteasyok.spcialsmp.manager.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        var dataManager = SpcialSmp.get().getPlayerDataManager();

        // 1. Fuel System: Naye players ko 24 ghante fuel dena
        if (!p.hasPlayedBefore()) {
            FuelManager.setFuel(p, 1440);
        }

        // 2. Task System: Check karna ki kya player ke paas task hai (Unban ke baad kaam aayega)
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            if (p.isOnline() && !hasTaskBook(p)) {
                TaskManager.giveRandomTask(p);
                p.sendMessage("§6§lSURVIVAL BOT §8» §fA new task has been assigned! Complete it to stay alive.");
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
        }, 100L); 

        // 3. Attribute Reset (Scale fix)
        if (p.getAttribute(Attribute.GENERIC_SCALE) != null) 
            p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);

        // 4. First Time Spin: Agar pehli baar join kiya hai toh Card milega
        if (!dataManager.hasReceivedFirstCard(p.getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
                 if (p.isOnline()) CardSpinner.openSpinGUI(p);
            }, 140L);
        }
    }

    // Task check logic (Anti-duplicate)
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
