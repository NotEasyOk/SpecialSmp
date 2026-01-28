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

        // 1. Fuel Logic: Naye players ko 24 ghante fuel dena
        if (!p.hasPlayedBefore()) {
            FuelManager.setFuel(p, 1440);
        }

        // 2. Task Logic: Check karke naya task dena (Unban ke baad bhi kaam karega)
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            if (p.isOnline() && !hasTaskBook(p)) {
                TaskManager.giveRandomTask(p);
                p.sendMessage("§6§lSURVIVAL BOT §8» §fA new task has been assigned! Complete it to survive.");
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
        }, 100L); 

        // 3. Reset Scale: Player size fix
        if (p.getAttribute(Attribute.GENERIC_SCALE) != null) 
            p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);

        // 4. First Time Card Spin: Video style animation
        if (!dataManager.hasReceivedFirstCard(p.getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
                 if (p.isOnline()) CardSpinner.openSpinGUI(p);
            }, 140L);
        }
    }

    // Task duplicate na ho uske liye check
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
