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
            FuelManager.setFuel(p, (15*3600) + (59*60) + 59); // 24 Hours initial fuel
        }

        // 2. Storm Cleanup (Important!)
        p.setAllowFlight(false); 
        p.setFlying(false);
        p.getWorld().getWorldBorder().setWarningDistance(0); 

        // 3. Task Logic (Line 35 se replace karein)
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            if (!p.isOnline() || !FuelManager.isSystemEnabled()) return;

            long currentTime = System.currentTimeMillis();
            long lastBookTime = dataManager.getLastBookTime(p.getUniqueId());
            
            // 24 Ghante ka check (86400000ms = 24h)
            if (currentTime - lastBookTime >= 86400000L) {
                if (!hasTaskBook(p)) {
                    TaskManager.giveRandomTask(p);
                    dataManager.setLastBookTime(p.getUniqueId(), currentTime); // Database mein save

                    p.sendMessage("§6§lSURVIVAL BOT §8» §fA new task assigned! Complete it to survive.");
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                }
            }
        }, 6000L); // 6000L = 5 Minutes delay (Sahi delay jo aapne maanga tha)

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
        // Wither Storm Cleanup (Ghost entities remove karne ke liye)
        for (Entity entity : event.getPlayer().getWorld().getEntities()) {
            // Agar Wither hai ya ItemDisplay/ArmorStand jo Storm ka ho sakta hai
            if (entity.getType() == org.bukkit.entity.EntityType.WITHER || 
                entity.getType() == org.bukkit.entity.EntityType.WITHER_SKELETON) {
                
                // Check agar wo Wither Storm ka part hai (Mod/Plugin specific)
                entity.remove(); 
            }
            
            // Purani Giant Swords hatane ke liye
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
