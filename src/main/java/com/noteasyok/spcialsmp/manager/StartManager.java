package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StartManager {

    private final SpcialSmp plugin;

    public StartManager(SpcialSmp plugin) {
        this.plugin = plugin;
    }

    public void runStartSequence() {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("smp-start-system.enabled")) return;

        World world = Bukkit.getWorlds().get(0);
        String starter = config.getString("smp-start-system.starter-name");

        // Set Initial Border
        world.getWorldBorder().setCenter(world.getSpawnLocation());
        world.getWorldBorder().setSize(config.getDouble("smp-start-system.border.initial-size"));

        // --- STAGE 1: THE SMP INITIATION ---
        new BukkitRunnable() {
            int timer = config.getInt("smp-start-system.timers.countdown-delay");

            @Override
            public void run() {
                // 5 Second Countdown Announcement on Screen
                if (timer <= 5 && timer > 0) {
                    String title = config.getString("smp-start-system.messages.countdown-title").replace("%time%", String.valueOf(timer));
                    String sub = config.getString("smp-start-system.messages.countdown-subtitle");

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(title, sub, 0, 21, 0);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                    }
                }

                if (timer <= 0) {
                    // Start Expanding World Border
                    double finalSize = config.getDouble("smp-start-system.border.final-size");
                    int duration = config.getInt("smp-start-system.border.expansion-duration");
                    world.getWorldBorder().setSize(finalSize, duration);

                    // Final Start Announcement on Screen
                    String sTitle = config.getString("smp-start-system.messages.startup-title");
                    String sSub = config.getString("smp-start-system.messages.startup-subtitle")
                            .replace("%starter%", starter)
                            .replace("%size%", String.valueOf((int)finalSize));

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(sTitle, sSub, 10, 100, 20);
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                        p.spawnParticle(Particle.EXPLOSION_HUGE, p.getLocation(), 5);
                    }
                    this.cancel();
                }
                timer--;
            }
        }.runTaskTimer(plugin, 0, 20);

        // --- STAGE 2: CARD SPIN ANNOUNCEMENT ---
        new BukkitRunnable() {
            @Override
            public void run() {
                new BukkitRunnable() {
                    int spinCountdown = 5;
                    @Override
                    public void run() {
                        if (spinCountdown > 0) {
                            String fTitle = config.getString("smp-start-system.messages.fate-warning-title");
                            String fSub = config.getString("smp-start-system.messages.fate-warning-subtitle").replace("%time%", String.valueOf(spinCountdown));

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendTitle(fTitle, fSub, 0, 21, 0);
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.2f);
                            }
                        } else {
                            // Give Items Announcement
                            String gTitle = config.getString("smp-start-system.messages.fate-grant-title");
                            String gSub = config.getString("smp-start-system.messages.fate-grant-subtitle");

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendTitle(gTitle, gSub, 10, 70, 20);
                                p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 1f);
                                p.spawnParticle(Particle.TOTEM, p.getLocation(), 100, 0.5, 1, 0.5, 0.2);
                                
                                // YOUR SPIN LOGIC HERE
                            }
                            this.cancel();
                        }
                        spinCountdown--;
                    }
                }.runTaskTimer(plugin, 0, 20);
            }
        }.runTaskLater(plugin, (config.getLong("smp-start-system.timers.item-distribution-delay") - 5) * 20L);
    }
          }
