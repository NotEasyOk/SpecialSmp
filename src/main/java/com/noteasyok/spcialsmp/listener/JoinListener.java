package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.PlayerDataManager;
import org.bukkit.configuration.file.FileConfiguration;
import com.noteasyok.spcialsmp.manager.CardSpinner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
        PlayerDataManager dataManager = SpcialSmp.get().getPlayerDataManager();
        FileConfiguration config = SpcialSmp.get().getConfig();
    
        // 2. Storm & Flight Cleanup
        p.setAllowFlight(false);
        p.setFlying(false);
        p.getWorld().getWorldBorder().setWarningDistance(0);
        cleanupWitherStormEntities(p);

        // --- Purana Task Logic Delete Kar Diya Gaya Hai ---

        // 4. Reset Scale
        if (p.getAttribute(Attribute.GENERIC_SCALE) != null)
            p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);

        // 5. CARD SPIN LOGIC
        boolean startSystemEnabled = config.getBoolean("smp-start-system.enabled");

        if (!startSystemEnabled) {
            if (!dataManager.hasReceivedFirstCard(p.getUniqueId())) {
                Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
                    if (p.isOnline() && !dataManager.hasReceivedFirstCard(p.getUniqueId())) {
                        CardSpinner.openSpinGUI(p);
                    }
                }, 140L);
            }
        } else {
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
        // Fuel save karne ka logic hata diya kyunki ab Fuel system nahi hai.
    }
                    }
