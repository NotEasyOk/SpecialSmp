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

        // --- STAGE 1: INITIAL COUNTDOWN (e.g. 10s) ---
        new BukkitRunnable() {
            int timer = config.getInt("smp-start-system.timers.countdown-delay");

            @Override
            public void run() {
                if (timer > 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle("§c§l" + timer, "§fPreparing World...", 0, 21, 0);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                    }
                } else {
                    // Border starts expanding
                    double finalSize = config.getDouble("smp-start-system.border.final-size");
                    int duration = config.getInt("smp-start-system.border.expansion-duration");
                    world.getWorldBorder().setSize(finalSize, duration);

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle("§a§lSTARTED!", "§fThe world is expanding...", 10, 70, 20);
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    }
                    
                    this.cancel();
                    // --- PHASE 2: WAIT FOR 120S THEN SPIN ---
                    startItemTimer(config); 
                }
                timer--;
            }
        }.runTaskTimer(plugin, 0, 20L); // 20L = 1 second interval
    }

    private void startItemTimer(FileConfiguration config) {
        long totalDelay = config.getLong("smp-start-system.timers.item-distribution-delay"); // 120s

        // Pehle 120s (minus 5s warning) tak wait karega
        new BukkitRunnable() {
            @Override
            public void run() {
                // Warning Countdown (Final 5s)
                new BukkitRunnable() {
                    int warning = 5;
                    @Override
                    public void run() {
                        if (warning > 0) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendTitle("§6§lFATE ARRIVING", "§eIn " + warning + "s...", 0, 21, 0);
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.2f);
                            }
                        } else {
                            // --- FINAL STEP: ANIMATION START ---
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                CardSpinner.openSpinGUI(p); // Spin trigger!
                                p.spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 100, 0.5, 1, 0.5, 0.2);
                            }
                            isRunning = false; // System free for next time
                            this.cancel();
                        }
                        warning--;
                    }
                }.runTaskTimer(plugin, 0, 20L);
            }
        }.runTaskLater(plugin, (totalDelay - 5) * 20L); // Asli wait yahan ho raha hai
    }
                        }
