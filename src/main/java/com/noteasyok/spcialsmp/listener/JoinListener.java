package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardSpinner;
import com.noteasyok.spcialsmp.manager.FuelManager;
import com.noteasyok.spcialsmp.manager.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.persistence.PersistentDataType;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        var dataManager = SpcialSmp.get().getPlayerDataManager();

        // 1. Fuel Logic (Only if System is ENABLED)
        if (!p.hasPlayedBefore() && FuelManager.isSystemEnabled()) {
            FuelManager.setFuel(p, 57599); // 24 Hours initial fuel
        }

        // 2. Storm Cleanup (Important!)
        p.setAllowFlight(false); 
        p.setFlying(false);
        p.getWorld().getWorldBorder().setWarningDistance(0); 

        // 3. Task Logic (Only if System is ENABLED)
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            if (!p.isOnline()) return;
            
            // FIX: Check if system is ON before giving Task Book
            if (!FuelManager.isSystemEnabled()) return;

            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "daily_task_day");
            long currentDay = System.currentTimeMillis() / 86400000L; 
            Long storedDay = p.getPersistentDataContainer().get(key, PersistentDataType.LONG);

            if (!hasTaskBook(p) && (storedDay == null || storedDay != currentDay)) {
                TaskManager.giveRandomTask(p);
                p.getPersistentDataContainer().set(key, PersistentDataType.LONG, currentDay);

                p.sendMessage("§6§lSURVIVAL BOT §8» §fA new task has been assigned! Complete it to survive.");
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
        }, 100L); 

        // 4. Reset Scale
        if (p.getAttribute(Attribute.GENERIC_SCALE) != null) 
            p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);

        // 5. First Time Card Spin
        if (!dataManager.hasReceivedFirstCard(p.getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
                 if (p.isOnline()) CardSpinner.openSpinGUI(p);
            }, 140L);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Purani atki hui swords hatane ka logic
        for (Entity entity : event.getPlayer().getNearbyEntities(100, 100, 100)) {
            if (entity instanceof ItemDisplay) {
                ItemDisplay display = (ItemDisplay) entity;
                // Sirf Netherite Sword (Giant Sword) ko target kar rahe hain
                if (display.getItemStack() != null && display.getItemStack().getType() == Material.NETHERITE_SWORD) {
                    entity.remove(); 
                }
            }
        }
    }
                
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        // FuelManager se current fuel lein
        int currentFuel = FuelManager.getFuel(p);
        long now = System.currentTimeMillis() / 1000;
        
        // DataManager mein turant save karein taaki offline drain calculation sahi ho
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
