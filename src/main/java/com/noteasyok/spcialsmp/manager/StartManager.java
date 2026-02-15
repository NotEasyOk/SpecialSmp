package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StartManager {

    private final SpcialSmp plugin;
    private boolean isRunning = false; // Fix 1: Double start rokne ke liye

    public StartManager(SpcialSmp plugin) {
        this.plugin = plugin;
    }

    public void runStartSequence(Player starter) { // Starter pass kiya center ke liye
        if (isRunning) {
            starter.sendMessage("§c§lERROR » §fSMP already starting!");
            return;
        }
        
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("smp-start-system.enabled")) return;

        isRunning = true;
        World world = starter.getWorld();

        // Fix 2: Border Center jidhar starter khada hai
        world.getWorldBorder().setCenter(starter.getLocation());
        world.getWorldBorder().setSize(config.getDouble("smp-start-system.border.initial-size"));

        // --- STAGE 1: COUNTDOWN ---
        new BukkitRunnable() {
            int timer = config.getInt("smp-start-system.timers.countdown-delay");

            @Override
            public void run() {
                if (timer <= 5 && timer > 0) {
                    String title = ChatColor.translateAlternateColorCodes('&', config.getString("smp-start-system.messages.countdown-title").replace("%time%", String.valueOf(timer)));
                    String sub = ChatColor.translateAlternateColorCodes('&', config.getString("smp-start-system.messages.countdown-subtitle"));

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(title, sub, 0, 21, 0);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                    }
                }

                if (timer <= 0) {
                    double finalSize = config.getDouble("smp-start-system.border.final-size");
                    int duration = config.getInt("smp-start-system.border.expansion-duration");
                    world.getWorldBorder().setSize(finalSize, duration); // Border expand start

                    String sTitle = ChatColor.translateAlternateColorCodes('&', config.getString("smp-start-system.messages.startup-title"));
                    String sSub = ChatColor.translateAlternateColorCodes('&', config.getString("smp-start-system.messages.startup-subtitle")
                            .replace("%starter%", starter.getName())
                            .replace("%size%", String.valueOf((int)finalSize)));

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(sTitle, sSub, 10, 70, 20);
                        CardSpinner.openSpinGUI(p);
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    }
                    
                    this.cancel(); // Fix 3: Loop rokne ke liye
                    startSpinSequence(config); // Agla stage shuru
                }
                timer--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void startSpinSequence(FileConfiguration config) {
        // Fix 4: 120 seconds (Ya config value) baad spin
        long delay = config.getLong("smp-start-system.timers.item-distribution-delay");
        
        new BukkitRunnable() {
            int spinCountdown = 5;
            @Override
            public void run() {
                if (spinCountdown > 0) {
                    String fTitle = ChatColor.translateAlternateColorCodes('&', config.getString("smp-start-system.messages.fate-warning-title"));
                    String fSub = ChatColor.translateAlternateColorCodes('&', config.getString("smp-start-system.messages.fate-warning-subtitle").replace("%time%", String.valueOf(spinCountdown)));

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(fTitle, fSub, 0, 21, 0);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.2f);
                    }
                } else {
                    // GIVE ITEMS
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle("§6§lCARDS GIVEN", "§fFate has been decided!", 10, 70, 20);
                        p.spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 100, 0.5, 1, 0.5, 0.2);
                        // YAHAN APNA CardRegistry.startSpin(p) CALL KARO
                    }
                    isRunning = false; // Reset taaki naya start ho sake
                    this.cancel();
                }
                spinCountdown--;
            }
        }.runTaskTimer(plugin, delay * 20L, 20L);
    }
                            }
