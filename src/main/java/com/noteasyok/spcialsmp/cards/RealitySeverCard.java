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
    public Material getMaterial() { return Material.ECHO_SHARD; } 
    @Override
    public int getModelData() { return 110; }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        return item != null && item.getType() == getMaterial() 
               && item.hasItemMeta() && item.getItemMeta().hasDisplayName() 
               && item.getItemMeta().getDisplayName().contains(getName());
    }

    // --- LEFT CLICK: WORLD CUTTING SLASH ---
    @Override
    public void leftClick(Player p) {
        if (!isHoldingCard(p)) return;
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "left")) return;

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2f, 0.5f);
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1f, 2f);

        Location start = p.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        new BukkitRunnable() {
            Location current = start.clone();
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 25 || !current.getBlock().isPassable()) { this.cancel(); return; }

                for (int j = 0; j < 2; j++) { // Extra speed
                    current.add(direction.clone().multiply(0.5));
                    
                    // --- CINEMATIC CRACK EFFECT ---
                    // Reality tearing apart particles (Ominous & Trial Spawner)
                    current.getWorld().spawnParticle(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, current, 5, 0.2, 0.2, 0.2, 0);
                    current.getWorld().spawnParticle(Particle.SWEEP_ATTACK, current, 2, 0.5, 0.5, 0.5, 0.1);
                    current.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, current, 3, 0.1, 0.1, 0.1, 0.05);

                    for (Entity e : current.getWorld().getNearbyEntities(current, 2, 2, 2)) {
                        if (e instanceof LivingEntity victim && !e.equals(p)) {
                            victim.damage(8.0, p); // MASSIVE DAMAGE
                            victim.getWorld().spawnParticle(Particle.FLASH, victim.getLocation(), 1);
                            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1f, 0.5f);
                            this.cancel();
                            return;
                        }
                    }
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 1);
        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "left");
    }

    // --- RIGHT CLICK: INFINITE GRAVITY COLLAPSE ---
    @Override
    public void rightClick(Player p) {
        if (!isHoldingCard(p)) return;
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "right")) return;

        var trace = p.getWorld().rayTraceEntities(p.getEyeLocation(), p.getEyeLocation().getDirection(), 30, 1.0, e -> !e.equals(p));
        
        if (trace != null && trace.getHitEntity() instanceof LivingEntity target) {
            p.sendMessage("§0§l[!] §d§lGRAVITY ANCHOR ON: §f" + target.getName());
            
            new BukkitRunnable() {
                int timer = 0;
                @Override
                public void run() {
                    if (timer++ > 160 || !target.isValid()) { 
                        target.setGravity(true);
                        this.cancel(); 
                        return; 
                    }
                    
                    // Forcefully Pin to ground or Hover Crush
                    target.setGravity(false);
                    target.setVelocity(new Vector(0, -0.2, 0)); // Constant pressure
                    
                    // Visual Vortex around victim
                    Location tLoc = target.getLocation();
                    for(int i=0; i<8; i++) {
                        double angle = i * Math.PI / 4;
                        double x = Math.cos(angle + timer*0.5) * 1.5;
                        double z = Math.sin(angle + timer*0.5) * 1.5;
                        tLoc.getWorld().spawnParticle(Particle.DUST, tLoc.clone().add(x, 1, z), 2, new DustOptions(Color.BLACK, 1.5f));
                    }
                    if(timer % 5 == 0) target.damage(2.0, p);
                }
            }.runTaskTimer(SpcialSmp.get(), 0, 1);

            SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "right");
        }
    }

    // --- SHIFT+RIGHT: CATACLYSMIC BARRAGE (Instant Lasers) ---
    @Override
    public void shiftRightClick(Player p) {
        if (!isHoldingCard(p)) return;
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "shift_right")) return;

        List<Entity> nearby = p.getNearbyEntities(20, 15, 20);
        if (nearby.isEmpty()) return;

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f);

        new BukkitRunnable() {
            int wave = 0;
            @Override
            public void run() {
                if (wave++ > 5) { this.cancel(); return; } // 5 Waves of strikes

                for (Entity e : nearby) {
                    if (e instanceof LivingEntity target && !e.equals(p)) {
                        Location loc = target.getLocation();
                        
                        // Instant Bolt Visual
                        for(int y=0; y<25; y++) {
                            loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, y, 0), 5, 0.1, 0.5, 0.1, new DustOptions(Color.fromRGB(150, 0, 255), 2.0f));
                        }
                        
                        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 2);
                        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 2f);
                        target.damage(4.0, p);
                        target.setVelocity(new Vector(0, -1, 0)); // Keep them down
                    }
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 10); // Faster waves

        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "shift_right");
    }
                            }
