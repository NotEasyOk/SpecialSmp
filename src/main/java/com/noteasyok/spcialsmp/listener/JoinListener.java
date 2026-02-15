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

        // 1. Fuel Logic & Setup
        boolean isFuelEnabled = config.getBoolean("fuel-system.enabled"); // Config check direct
        if (!p.hasPlayedBefore() && isFuelEnabled) {
            FuelManager.setFuel(p, (15 * 3600) + (59 * 60) + 59);
        }

        // 2. Storm & Flight Cleanup
        p.setAllowFlight(false);
        p.setFlying(false);
        p.getWorld().getWorldBorder().setWarningDistance(0);
        cleanupWitherStormEntities(p);

        // 3. Task Logic (FIXED: Message only if Fuel System is ON)
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            // BUG FIX: Fuel system OFF hai to Task wala message ya book nahi aayegi
            if (!p.isOnline() || !isFuelEnabled) return;

            long currentTime = System.currentTimeMillis();
            long lastBookTime = dataManager.getLastBookTime(p.getUniqueId());
            
            if (currentTime - lastBookTime >= 86400000L) {
                if (!hasTaskBook(p)) {
                    TaskManager.giveRandomTask(p);
                    dataManager.setLastBookTime(p.getUniqueId(), currentTime);
                    p.sendMessage("§6§lSURVIVAL BOT §8» §fA new task assigned!");
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                }
            }
        }, 1200L); // 1 minute delay (zyada lamba nahi)

        // 4. Reset Scale
        if (p.getAttribute(Attribute.GENERIC_SCALE) != null)
            p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);

        // 5. CARD SPIN LOGIC (FIXED: No double cards, No double messages)
        boolean startSystemEnabled = config.getBoolean("smp-start-system.enabled");

        if (!startSystemEnabled) {
            // FALSE: Join par tabhi spin hoga agar use aaj tak card na mila ho
            if (!dataManager.hasReceivedFirstCard(p.getUniqueId())) {
                Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
                    // Re-check online status and card status to prevent double spin
                    if (p.isOnline() && !dataManager.hasReceivedFirstCard(p.getUniqueId())) {
                        CardSpinner.openSpinGUI(p);
                    }
                }, 140L);
            }
        } else {
            // TRUE: Wait for admin
            p.sendMessage("§e§lSMP §8» §fWait for the Admin to initiate the sequence...");
        }
    }

    private void cleanupWitherStormEntities(Player p) {
        for (Entity entity : p.getWorld().getEntities()) {
            if (entity.getType() == org.bukkit.entity.EntityType.WITHER || 
                entity.getType() == org.bukkit.entity.EntityType.WITHER_SKELETON) {
                entity.remove();
            }
            if (entity instanceof ItemDisplay display) {
                ItemStack item = display.getItemStack();
                if (item != null && item.getType() == Material.NETHERITE_SWORD) {
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
