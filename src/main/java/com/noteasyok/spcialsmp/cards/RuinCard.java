package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuinCard extends BaseCard implements Listener {

    private final Map<String, Long> cooldowns = new HashMap<>();
    private final String DIM_NAME = "ruin_dimension";
    private final Map<Player, BossBar> activeBars = new HashMap<>();

    public RuinCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override
    public String getName() { return "Ruin Card"; }
    @Override
    public int getModelData() { return 7; }
    @Override
    public Material getMaterial() { return Material.GRAY_DYE; }

    // --- LEFT CLICK: AIM-BASED PORTAL ---
    @Override
    public void leftClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.ruin.dimension_cd", 120);
        if (!isCool(p, "dimension", cd)) return;

        // Player kahan dekh raha hai (Target Block)
        Block targetBlock = p.getTargetBlock(null, 5); // 5 blocks door tak portal khulega
        if (targetBlock.getType() == Material.AIR) {
            p.sendMessage(ChatColor.RED + "Look at a block to open the portal!");
            return;
        }

        Location portalLoc = targetBlock.getLocation().add(0.5, 1, 0.5);
        p.sendMessage(ChatColor.GREEN + "Portal opening at your aim...");

        // Portal Visual (Purple Circle at Aim)
        new BukkitRunnable() {
            int timer = 0;
            @Override
            public void run() {
                if (timer >= 40) { // 2 seconds animation
                    teleportToDimension(p, portalLoc);
                    this.cancel();
                    return;
                }
                // Circle Particles around aim
                for (double i = 0; i < Math.PI * 2; i += Math.PI / 8) {
                    double x = Math.cos(i) * 1.2;
                    double z = Math.sin(i) * 1.2;
                    portalLoc.getWorld().spawnParticle(Particle.PORTAL, portalLoc.clone().add(x, 0.5, z), 5, 0, 0, 0, 0.1);
                    portalLoc.getWorld().spawnParticle(Particle.SPORE_BLOSSOM_AIR, portalLoc.clone().add(x, 0.5, z), 2);
                }
                timer += 5;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 5L);
    }

    private void teleportToDimension(Player p, Location oldLoc) {
        World ruinWorld = Bukkit.getWorld(DIM_NAME);
        if (ruinWorld == null) {
            WorldCreator wc = new WorldCreator(DIM_NAME);
            wc.type(WorldType.AMPLIFIED); // Photo jaisa terrain
            ruinWorld = wc.createWorld();
        }

        Location targetLoc = ruinWorld.getHighestBlockAt(0, 0).getLocation().add(0, 1, 0);
        p.teleport(targetLoc);
        p.playSound(p.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1f, 1f);

        // BossBar Setup for this specific player
        BossBar bar = Bukkit.createBossBar(ChatColor.DARK_GREEN + "Toxic Dimension", BarColor.GREEN, BarStyle.SOLID);
        bar.addPlayer(p);
        bar.setVisible(true);
        activeBars.put(p, bar);

        new BukkitRunnable() {
            int timeLeft = 60;
            @Override
            public void run() {
                if (timeLeft <= 0 || !p.isOnline() || !p.getWorld().getName().equals(DIM_NAME)) {
                    p.teleport(oldLoc);
                    bar.removePlayer(p);
                    activeBars.remove(p);
                    this.cancel();
                    return;
                }

                bar.setProgress(timeLeft / 60.0);
                bar.setTitle(ChatColor.DARK_GREEN + "Ruin Collapse in: " + timeLeft + "s");
                
                // Toxic Mist Particles
                p.spawnParticle(Particle.FALLING_SPORE_BLOSSOM, p.getLocation(), 40, 7, 7, 7);
                
                timeLeft--;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    // --- RIGHT CLICK: DARK SHIELD ---
    @Override
    public void rightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.ruin.aura_cd", 40);
        if (!isCool(p, "aura", cd)) return;

        p.sendMessage(ChatColor.DARK_GRAY + "Shadow Shield Active!");
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 200) { this.cancel(); return; }
                p.getWorld().spawnParticle(Particle.SQUID_INK, p.getLocation().add(0, 1, 0), 12, 0.6, 0.8, 0.6, 0.02);
                ticks += 10;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 10L);
    }

    // --- SHIFT CLICK: SILVERFISH ARMY ---
    @Override
    public void shiftRightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.ruin.summon_cd", 50);
        if (!isCool(p, "summon", cd)) return;

        for (int i = 0; i < 10; i++) {
            Silverfish s = p.getWorld().spawn(p.getLocation(), Silverfish.class);
            s.setCustomName(ChatColor.RED + p.getName() + "'s Minion");
            s.setCustomNameVisible(true);
            Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), s::remove, 400L);
        }
    }

    // --- POISON FLOOR EVENT ---
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.getWorld().getName().equals(DIM_NAME)) {
            if (p.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR) {
                if (p.getTicksLived() % 20 == 0) {
                    p.damage(1.0);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                }
            }
        }
    }

    private boolean isCool(Player p, String key, int seconds) {
        long now = System.currentTimeMillis();
        String mapKey = p.getUniqueId().toString() + "_" + key;
        if (cooldowns.containsKey(mapKey) && cooldowns.get(mapKey) > now) {
            p.sendMessage(ChatColor.RED + "Cooldown: " + ((cooldowns.get(mapKey) - now) / 1000) + "s");
            return false;
        }
        cooldowns.put(mapKey, now + (seconds * 1000L));
        return true;
    }
            }
