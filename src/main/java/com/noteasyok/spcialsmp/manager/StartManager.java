package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StartManager {

    private final SpcialSmp plugin;
    private boolean isRunning = false;

    public StartManager(SpcialSmp plugin) {
        this.plugin = plugin;
    }

    public void runStartSequence(Player starter) {
        if (isRunning) {
            starter.sendMessage("§c§lERROR » §fSMP already starting!");
            return;
        }
        
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("smp-start-system.enabled")) return;

        isRunning = true;
        World world = starter.getWorld();

        // Border Setup
        world.getWorldBorder().setCenter(starter.getLocation());
        world.getWorldBorder().setSize(config.getDouble("smp-start-system.border.initial-size"));

        // --- STAGE 1: INITIAL COUNTDOWN (e.g. 5 Seconds) ---
        new BukkitRunnable() {
            int timer = config.getInt("smp-start-system.timers.countdown-delay");

            @Override
            public void run() {
                if (timer > 0) {
                    String title = ChatColor.translateAlternateColorCodes('&', config.getString("smp-start-system.messages.countdown-title").replace("%time%", String.valueOf(timer)));
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(title, "§fPreparing World...", 0, 21, 0);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                    }
                }

                if (timer <= 0) {
                    // Border starts expanding
                    double finalSize = config.getDouble("smp-start-system.border.final-size");
                    int duration = config.getInt("smp-start-system.border.expansion-duration");
                    world.getWorldBorder().setSize(finalSize, duration);

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle("§a§lSTARTED!", "§fWait for fate to decide your card...", 10, 70, 20);
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    }
                    
                    this.cancel();
                    // AB 120 SECONDS KA WAIT SHURU HOGA (Stage 2)
                    startSpinSequence(config); 
                }
                timer--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void startSpinSequence(FileConfiguration config) {
        // Yahan se 120 seconds ki ginti shuru hoti hai
        long totalWait = config.getLong("smp-start-system.timers.item-distribution-delay");
        
        new BukkitRunnable() {
            int warningTimer = 5; // Fate warning countdown
            
            @Override
            public void run() {
                if (warningTimer > 0) {
                    String fTitle = ChatColor.translateAlternateColorCodes('&', config.getString("smp-start-system.messages.fate-warning-title"));
                    String fSub = ChatColor.translateAlternateColorCodes('&', config.getString("smp-start-system.messages.fate-warning-subtitle").replace("%time%", String.valueOf(warningTimer)));

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(fTitle, fSub, 0, 21, 0);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.2f);
                    }
                } else {
                    // --- FINAL STEP: ANIMATION START ---
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        // Card Spinner trigger kar rahe hain sabke liye
                        CardSpinner.openSpinGUI(p); 
                        
                        p.spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 100, 0.5, 1, 0.5, 0.2);
                    }
                    isRunning = false; // Reset for next time
                    this.cancel();
                }
                warningTimer--;
            }
            // Ye 120s baad chalega (minus 5s for the fate warning)
        }.runTaskTimer(plugin, (totalWait - 5) * 20L, 20L);
    }
                         }
