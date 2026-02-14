package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CooldownManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GravityCard extends BaseCard {

    public GravityCard() {
        super("Gravity Card", Material.ECHO_SHARD, // 1.21 Item (Unique texture)
                "§7Control the fundamental forces.",
                " ",
                "§6§lABILITY 1: §eGravity Push §7(Left Click)",
                "§fAim at an enemy to launch them",
                "§finto the sky with zero gravity.",
                " ",
                "§6§lABILITY 2: §5Black Hole §7(Right Click)",
                "§fSummon a gravity vortex that pulls",
                "§fand spins enemies in the air.",
                " ",
                "§6§lULTIMATE: §dZero-G Zone §7(Shift + Right)",
                "§fCreate a zone where gravity fails.",
                " ",
                "§c§l(!) §7Owner is immune to effects.");
    }

    @Override
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action a = e.getAction();

        // --- ABILITY 1: GRAVITY PUSH (Left Click) ---
        if (a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK) {
            if (CooldownManager.checkCooldown(p, "Gravity Card Left")) {
                performGravityPush(p);
                CooldownManager.setCooldown(p, "Gravity Card Left", 3); // 3 Seconds
            }
        }

        // --- ABILITY 2: BLACK HOLE TORNADO (Right Click) ---
        else if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
            if (p.isSneaking()) {
                // --- ABILITY 3: ZERO-G ZONE (Shift + Right) ---
                if (CooldownManager.checkCooldown(p, "Gravity Card Shift")) {
                    performZeroGravityZone(p);
                    CooldownManager.setCooldown(p, "Gravity Card Shift", 20); // 20 Seconds
                }
            } else {
                // Normal Right Click
                if (CooldownManager.checkCooldown(p, "Gravity Card Right")) {
                    performBlackHole(p);
                    CooldownManager.setCooldown(p, "Gravity Card Right", 10); // 10 Seconds
                }
            }
        }
    }

    // --- LOGIC 1: GRAVITY PUSH (Aim & Launch) ---
    private void performGravityPush(Player p) {
        // Raytrace to find target (Aim based)
        Entity target = getTargetEntity(p, 20);

        if (target != null && target instanceof LivingEntity && !target.equals(p)) {
            LivingEntity victim = (LivingEntity) target;

            // Visuals
            p.getWorld().spawnParticle(Particle.SONIC_BOOM, p.getEyeLocation(), 1); // 1.21 Particle
            p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 1.5f);

            // Physics (Launch away and up)
            Vector dir = p.getLocation().getDirection().normalize();
            victim.setVelocity(dir.multiply(2.5).setY(1.2)); // Hawa mein faink dega

            p.sendMessage("§5§lGRAVITY » §fYeeted §d" + victim.getName() + "§f!");
        } else {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            p.sendMessage("§cNo target found in range!");
        }
    }

    // --- LOGIC 2: BLACK HOLE TORNADO (Pull & Spin) ---
    private void performBlackHole(Player p) {
        // Target block jahan dekh raha hai
        Location targetLoc = p.getTargetBlock(null, 25).getLocation().add(0, 2, 0);
        
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);
        p.sendMessage("§5§lGRAVITY » §fBlack Hole opened!");

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) { // 5 Seconds chalega
                    this.cancel();
                    targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
                    targetLoc.getWorld().spawnParticle(Particle.EXPLOSION, targetLoc, 5);
                    return;
                }

                // Animation: Spiral Particles
                double radius = 3.5;
                double x = radius * Math.cos(ticks * 0.2);
                double z = radius * Math.sin(ticks * 0.2);
                targetLoc.getWorld().spawnParticle(Particle.WITCH, targetLoc.clone().add(x, 0, z), 1);
                targetLoc.getWorld().spawnParticle(Particle.WITCH, targetLoc.clone().add(-x, 0, -z), 1);
                
                // Center Core
                targetLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, targetLoc, 2);

                // Logic: Pull Entities
                for (Entity e : targetLoc.getWorld().getNearbyEntities(targetLoc, 6, 6, 6)) {
                    if (e instanceof LivingEntity && !e.equals(p)) { // Owner safe
                        Location eLoc = e.getLocation();
                        // Vector towards center
                        Vector pull = targetLoc.toVector().subtract(eLoc.toVector()).normalize().multiply(0.4);
                        // Add spin upwards
                        pull.setY(0.3);
                        e.setVelocity(pull);
                    }
                }
                ticks++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    // --- LOGIC 3: ZERO GRAVITY ZONE (Floating Area) ---
    private void performZeroGravityZone(Player p) {
        Location center = p.getLocation();
        p.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1f, 0.8f);

        new BukkitRunnable() {
            int duration = 0;
            @Override
            public void run() {
                if (duration >= 160) { // 8 Seconds
                    this.cancel();
                    return;
                }

                // Visual: Dome Effect
                for (int i = 0; i < 10; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double rad = 5;
                    double x = Math.cos(angle) * rad;
                    double z = Math.sin(angle) * rad;
                    center.getWorld().spawnParticle(Particle.DRAGON_BREATH, center.clone().add(x, 0.5, z), 0);
                }

                // Logic: Anti-Gravity for everyone inside
                for (Entity e : center.getWorld().getNearbyEntities(center, 5, 5, 5)) {
                    if (e instanceof LivingEntity && !e.equals(p)) {
                        LivingEntity le = (LivingEntity) e;
                        le.setGravity(false); // Gravity OFF
                        le.setVelocity(new Vector(0, 0.05, 0)); // Slow float up
                        le.setFallDistance(0);
                    }
                }
                
                // Cleanup: Gravity wapas lana padega end mein
                if (duration == 159) {
                     for (Entity e : center.getWorld().getNearbyEntities(center, 6, 6, 6)) {
                        if (e instanceof LivingEntity) ((LivingEntity) e).setGravity(true);
                     }
                }
                
                duration++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    // --- UTILS: Aim Assist ---
    private Entity getTargetEntity(Player p, int range) {
        List<Entity> nearby = p.getNearbyEntities(range, range, range);
        Entity target = null;
        double minDistance = Double.MAX_VALUE;
        Vector targetVec = p.getLocation().getDirection();

        for (Entity e : nearby) {
            if (e instanceof LivingEntity && !e.equals(p)) {
                Vector toEntity = e.getLocation().toVector().subtract(p.getLocation().toVector());
                // Check if aiming at entity (Dot Product)
                if (targetVec.clone().normalize().dot(toEntity.normalize()) > 0.95) { 
                    double dist = p.getLocation().distance(e.getLocation());
                    if (dist < minDistance) {
                        minDistance = dist;
                        target = e;
                    }
                }
            }
        }
        return target;
    }
    }
