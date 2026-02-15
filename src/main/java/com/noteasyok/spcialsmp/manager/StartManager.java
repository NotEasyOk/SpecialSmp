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
            starter.sendMessage("§c§lERROR » §fSequence already in progress!");
            return;
        }
        
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("smp-start-system.enabled")) return;

        isRunning = true;
        World world = starter.getWorld();

        // Border Setup
        world.getWorldBorder().setCenter(starter.getLocation());
        world.getWorldBorder().setSize(config.getDouble("smp-start-system.border.initial-size"));

        // --- STAGE 1: SMART COUNTDOWN ---
        new BukkitRunnable() {
            int timer = config.getInt("smp-start-system.timers.countdown-delay");

            @Override
            public void run() {
                // Aakhri 5 seconds mein config se message uthayega
                if (timer <= 5 && timer > 0) {
                    String title = color(config.getString("smp-start-system.messages.countdown-title").replace("%time%", String.valueOf(timer)));
                    String sub = color(config.getString("smp-start-system.messages.countdown-subtitle"));

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(title, sub, 0, 21, 0);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                    }
                } else if (timer > 5) {
                    starter.sendActionBar("§eSetup in progress: §f" + timer + "s left");
                }

                if (timer <= 0) {
                    double finalSize = config.getDouble("smp-start-system.border.final-size");
                    int duration = config.getInt("smp-start-system.border.expansion-duration");
                    world.getWorldBorder().setSize(finalSize, duration);

                    // Startup Titles from Config
                    String sTitle = color(config.getString("smp-start-system.messages.startup-title"));
                    String sSub = color(config.getString("smp-start-system.messages.startup-subtitle"));

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(sTitle, sSub, 10, 70, 20);
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    }
                    
                    this.cancel();
                    startItemTimer(config, world); 
                }
                timer--;
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    private void startItemTimer(FileConfiguration config, World world) {
        long totalDelay = config.getLong("smp-start-system.timers.item-distribution-delay");

        new BukkitRunnable() {
            @Override
            public void run() {
                new BukkitRunnable() {
                    int warning = 5;
                    @Override
                    public void run() {
                        if (warning > 0) {
                            // Fate Titles from Config
                            String fTitle = color(config.getString("smp-start-system.messages.fate-warning-title"));
                            String fSub = color(config.getString("smp-start-system.messages.fate-warning-subtitle").replace("%time%", String.valueOf(warning)));

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendTitle(fTitle, fSub, 0, 21, 0);
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.2f);
                            }
                        } else {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                CardSpinner.openSpinGUI(p);
                                p.spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 100, 0.5, 1, 0.5, 0.2);
                            }
                            isRunning = false;
                            this.cancel();
                        }
                        warning--;
                    }
                }.runTaskTimer(plugin, 0, 20L);
            }
        }.runTaskLater(plugin, (totalDelay - 5) * 20L);
    }

    // Helper method to support color codes (&)
    private String color(String msg) {
        if (msg == null) return "";
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
                }
