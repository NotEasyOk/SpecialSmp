package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardSpinner;
import com.noteasyok.spcialsmp.manager.FuelManager;
import com.noteasyok.spcialsmp.manager.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        var dataManager = SpcialSmp.get().getPlayerDataManager();
        var config = SpcialSmp.get().getConfig();

        // 1. Fuel Logic
        if (!p.hasPlayedBefore() && FuelManager.isSystemEnabled()) {
            FuelManager.setFuel(p, (15 * 3600) + (59 * 60) + 59);
        }

        // 2. Storm & Flight Cleanup
        p.setAllowFlight(false);
        p.setFlying(false);
        p.getWorld().getWorldBorder().setWarningDistance(0);
        cleanupWitherStormEntities(p); // Method niche banaya hai

        // 3. Task Logic (5 Min Delay)
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            if (!p.isOnline() || !FuelManager.isSystemEnabled()) return;
            long currentTime = System.currentTimeMillis();
            long lastBookTime = dataManager.getLastBookTime(p.getUniqueId());
            if (currentTime - lastBookTime >= 86400000L) {
                if (!hasTaskBook(p)) {
                    TaskManager.giveRandomTask(p);
                    dataManager.setLastBookTime(p.getUniqueId(), currentTime);
                    p.sendMessage("§6§lSURVIVAL BOT §8» §fA new task assigned!");
                }
            }
        }, 6000L);

        // 4. Reset Scale
        if (p.getAttribute(Attribute.GENERIC_SCALE) != null)
            p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);

        // 5. CARD SPIN LOGIC (THE FIX)
        boolean startSystemEnabled = config.getBoolean("smp-start-system.enabled");

        if (!startSystemEnabled) {
            // Agar system FALSE hai, toh purana logic: Join par card check
            if (!dataManager.hasReceivedFirstCard(p.getUniqueId())) {
                Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
                    if (p.isOnline()) CardSpinner.openSpinGUI(p);
                }, 140L);
            }
        } else {
            // Agar system TRUE hai, toh join par card NAHI milega.
            // Admin jab /smp start marega, tab StartManager handle karega.
            p.sendMessage("§e§lSMP §8» §fWaiting for the Admin to initiate the sequence...");
        }
    }

    // Alag se cleanup method taaki code saaf dikhe
    private void cleanupWitherStormEntities(Player p) {
        for (Entity entity : p.getWorld().getEntities()) {
            if (entity.getType() == org.bukkit.entity.EntityType.WITHER || 
                entity.getType() == org.bukkit.entity.EntityType.WITHER_SKELETON) {
                entity.remove();
            }
            if (entity instanceof ItemDisplay display) {
                if (display.getItemStack() != null && display.getItemStack().getType() == Material.NETHERITE_SWORD) {
                    entity.remove();
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        int currentFuel = FuelManager.getFuel(p);
        long now = System.currentTimeMillis() / 1000;
        SpcialSmp.get().getPlayerDataManager().setFuel(p.getUniqueId(), currentFuel);
        SpcialSmp.get().getPlayerDataManager().setLastLogout(p.getUniqueId(), now);
    }

    private boolean hasTaskBook(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                if (item.hasItemMeta() && item.getItemMeta().getDisplayName() != null &&
                        item.getItemMeta().getDisplayName().contains("Aaj ka task:")) {
                    return true;
                }
            }
        }
        return false;
    }
                        }
