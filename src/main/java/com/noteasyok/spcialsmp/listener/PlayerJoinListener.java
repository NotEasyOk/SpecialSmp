package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.manager.TaskManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Check karna ki kya player ke paas pehle se Task Book hai
        boolean hasTask = false;
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                if (item.hasItemMeta() && item.getItemMeta().getDisplayName().startsWith("§6§lAaj ka task:")) {
                    hasTask = true;
                    break;
                }
            }
        }

        // Agar unban hokar aaya hai aur task nahi hai, toh naya task do
        if (!hasTask) {
            TaskManager.giveRandomTask(p);
            p.sendMessage("§6§lSURVIVAL BOT §8» §fWelcome back! A new task has been assigned to you.");
        }
    }
                    }
