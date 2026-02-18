package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustOptions;

import java.util.List;

public class RealitySeverCard extends BaseCard {

    @Override
    public String getName() { return "Reality Sever Card"; }
    @Override
    public Material getMaterial() { return Material.NETHER_STAR; } 
    @Override
    public int getModelData() { return 110; } // Custom Model ID

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        return item != null && item.getType() == getMaterial() 
               && item.hasItemMeta() && item.getItemMeta().hasDisplayName() 
               && item.getItemMeta().getDisplayName().contains(getName());
    }

    // --- LEFT CLICK: DIMENSION SLASH (Moving Blade) ---
    @Override
    public void leftClick(Player p) {
        if (!isHoldingCard(p)) return;
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "left")) return;

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 1.5f); // Sharp sound

        Location start = p.getEyeLocation().subtract(0, 0.2, 0);
        Vector direction = start.getDirection().normalize().multiply(1.5); // Fast speed

        new BukkitRunnable() {
            Location current = start.clone();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 20 || !current.getBlock().isPassable()) { // Range: 30 blocks
                    this.cancel();
                    return;
                }

                current.add(direction);

                // --- VISUAL: SPINNING BLADE EFFECT ---
                // Vertical Slash Line
                current.getWorld().spawnParticle(Particle.SWEEP_ATTACK, current, 1);
                
                // Magical Glow (Cyan & Purple)
                current.getWorld().spawnParticle(Particle.DUST, current, 3, 0.2, 0.2, 0.2, new DustOptions(Color.AQUA, 1.0f));
                current.getWorld().spawnParticle(Particle.DUST, current, 3, 0.2, 0.2, 0.2, new DustOptions(Color.FUCHSIA, 1.0f));

                // --- HIT DETECTION ---
                for (Entity e : current.getWorld().getNearbyEntities(current, 1.5, 1.5, 1.5)) {
                    if (e instanceof LivingEntity && !e.equals(p)) {
                        LivingEntity victim = (LivingEntity) e;
                        victim.damage(12.0, p); // Heavy Damage
                        victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 10);
                        victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);
                        
                        // Knockback (Push away from slash)
                        victim.setVelocity(direction.clone().normalize().multiply(1.2).setY(0.2));
                        
                        this.cancel(); // Blade stops on hit
                        return;
                    }
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 1);

        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "left");
    }

    // --- RIGHT CLICK: GRAVITY CRUSH (Telekinetic Slam) ---
    @Override
    public void rightClick(Player p) {
        if (!isHoldingCard(p)) return;
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "right")) return;

        // Aim Assist (RayTrace)
        var trace = p.getWorld().rayTraceEntities(p.getEyeLocation(), p.getEyeLocation().getDirection(), 25, 1.0, e -> !e.equals(p));
        
        if (trace != null && trace.getHitEntity() instanceof LivingEntity target) {
            // Visual Beam connecting Player -> Target
            Location pLoc = p.getEyeLocation().subtract(0, 0.5, 0);
            Location tLoc = target.getEyeLocation();
            double dist = pLoc.distance(tLoc);
            Vector dir = tLoc.toVector().subtract(pLoc.toVector()).normalize();
            
            for (double i = 0; i < dist; i += 0.5) {
                p.getWorld().spawnParticle(Particle.SONIC_BOOM, pLoc.clone().add(dir.clone().multiply(i)), 1, 0, 0, 0, 0);
            }

            p.sendMessage("§b§l[!] §fCrushing §3" + target.getName());
            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.5f);

            // Step 1: Lift & Freeze
            target.setVelocity(new Vector(0, 0.8, 0)); // Lift up
            target.setGravity(false); // Disable gravity (Paper/Spigot feature)
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (target.isValid()) {
                        // Step 2: SLAM DOWN
                        target.setGravity(true);
                        target.setVelocity(new Vector(0, -3.0, 0)); // Rocket down
                        target.getWorld().spawnParticle(Particle.LARGE_EXPLOSION, target.getLocation(), 1);
                        target.damage(6.0, p); // Fall damage extra
                        p.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
                    }
                }
            }.runTaskLater(SpcialSmp.get(), 15); // Wait 0.75 seconds before slam

            SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "right");
        } else {
            p.sendMessage("§c[!] No target found nearby.");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
        }
    }

    // --- SHIFT+RIGHT: ORBITAL LASER BARRAGE ---
    @Override
    public void shiftRightClick(Player p) {
        if (!isHoldingCard(p)) return;
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "shift_right")) return;

        p.sendMessage("§d§l[!] §5Initiating Orbital Strike...");
        
        // Find targets around owner
        List<Entity> nearby = p.getNearbyEntities(15, 10, 15);
        if (nearby.isEmpty()) {
            p.sendMessage("§7No targets for Orbital Strike.");
            return;
        }

        new BukkitRunnable() {
            int strikes = 0;
            @Override
            public void run() {
                if (strikes++ >= 8 || nearby.isEmpty()) { // Max 8 strikes
                    this.cancel();
                    return;
                }

                // Pick random enemy
                Entity target = nearby.get((int) (Math.random() * nearby.size()));
                if (target instanceof LivingEntity && !target.equals(p)) {
                    Location strikeLoc = target.getLocation();
                    
                    // Visual: Laser from sky
                    for (int y = 0; y < 20; y++) {
                        strikeLoc.getWorld().spawnParticle(Particle.DUST, strikeLoc.clone().add(0, y, 0), 1, 0.1, 0, 0.1, new DustOptions(Color.PURPLE, 2));
                    }
                    
                    // Hit Effect
                    strikeLoc.getWorld().spawnParticle(Particle.FLASH, strikeLoc, 1);
                    strikeLoc.getWorld().playSound(strikeLoc, Sound.ITEM_TRIDENT_THUNDER, 5f, 1f);
                    ((LivingEntity) target).damage(5.0, p);
                    
                    // Remove from list so we don't spam same guy instantly (optional)
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 5); // Strike every 0.25 seconds

        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "shift_right");
    }
                                                     }
