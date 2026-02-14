package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PoseidonCard extends BaseCard implements Listener {

    public PoseidonCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override
    public String getName() {
        return "Poseidon Card";
    }

    @Override
    public int getModelData() {
        return 6; // Resource Pack ID
    }

    @Override
    public Material getMaterial() {
        return Material.PRISMARINE_SHARD; // Item Material
    }

    // --- COLORFUL LORE (Item Description) ---
    public List<String> getLore() {
        List<String> lore = new ArrayList<>();
        lore.add("§7");
        lore.add("§b§l🌊 ABILITIES:");
        lore.add("§fLeft-Click: §3Tidal Beam §7(Aim & Shoot)");
        lore.add("§fRight-Click: §9Water Prison §7(Traps Enemy)");
        lore.add("§fShift-Right: §bOcean's Wrath §7(God Mode)");
        lore.add("§7");
        lore.add("§3§oThe power of the seven seas");
        lore.add("§3§oresides within this shard...");
        return lore;
    }

    /* ---------------- LEFT CLICK: TIDAL BEAM (Aim Based) ---------------- */
    @Override
    public void leftClick(Player p) {
        if (!isCool(p, "left")) return;

        // 1. RayTrace (Aiming System - 25 Blocks)
        Location eye = p.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        
        // 2. Animation (Water Beam travels forward)
        new BukkitRunnable() {
            double distance = 0;
            final Location current = eye.clone();

            @Override
            public void run() {
                if (distance > 25) { // Max range 25 blocks
                    this.cancel();
                    return;
                }

                // Move point forward
                current.add(direction.clone().multiply(1));
                distance += 1;

                // Spawn Particles (Water Beam effect)
                p.getWorld().spawnParticle(Particle.SPLASH, current, 5, 0.1, 0.1, 0.1, 0.05);
                p.getWorld().spawnParticle(Particle.DRIPPING_WATER, current, 5, 0.2, 0.2, 0.2, 0.05);

                // Hit Detection
                if (!current.getBlock().isPassable()) { // Wall hit
                    p.getWorld().playSound(current, Sound.ENTITY_GENERIC_SPLASH, 1f, 1f);
                    this.cancel();
                    return;
                }

                for (org.bukkit.entity.Entity e : p.getWorld().getNearbyEntities(current, 1, 1, 1)) {
                    if (e instanceof LivingEntity target && e != p) {
                        // HIT! Damage & Knockback
                        target.damage(8, p);
                        target.setVelocity(direction.multiply(1.2).setY(0.4)); // Push back
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1f, 1f);
                        p.sendMessage(ChatColor.AQUA + "Hit " + target.getName() + " with Tidal Beam!");
                        this.cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);

        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THROW, 1f, 0.5f);
        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "left");
    }

    /* ---------------- RIGHT CLICK: WATER PRISON (Animation Box) ---------------- */
    @Override
    public void rightClick(Player p) {
        if (!isCool(p, "right")) return;

        // 1. Aim to find target (15 blocks range)
        RayTraceResult result = p.getWorld().rayTraceEntities(p.getEyeLocation(), p.getEyeLocation().getDirection(), 15, 1.5, e -> e != p && e instanceof LivingEntity);

        if (result != null && result.getHitEntity() instanceof LivingEntity target) {
            // Target Found: Trap them!
            trapEntityInWaterBox(target);
            p.sendMessage(ChatColor.BLUE + "Trapped " + target.getName() + " in a Water Prison!");
            p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);
        } else {
            // No Target: Fail Safe
            p.sendMessage(ChatColor.RED + "You must look at an enemy to trap them!");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return; // Don't apply cooldown if missed
        }

        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "right");
    }

    /* ---------------- SHIFT + RIGHT: OCEAN'S WRATH (Ultimate) ---------------- */
    @Override
    public void shiftRightClick(Player p) {
        if (!isCool(p, "shift_right")) return;

        // Buffs
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1)); // Strength 2
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1)); // Speed 2
        p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 200, 0)); 
        p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 200, 0));

        // Spiral Animation around player
        new BukkitRunnable() {
            double t = 0;
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 60) { // 3 Seconds animation
                    this.cancel();
                    return;
                }
                t += Math.PI / 8;
                double x = 1.5 * Math.cos(t);
                double z = 1.5 * Math.sin(t);
                Location loc = p.getLocation().add(x, 1, z);
                p.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 5, 0, 0, 0, 0);
                p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc.add(0, 0.5, 0), 1, 0, 0, 0, 0);
                ticks++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);

        p.getWorld().playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1f, 1f);
        p.sendMessage(ChatColor.DARK_AQUA + "§lOCEAN'S WRATH UNLEASHED!");

        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "shift_right");
    }

    /* ---------------- HELPER: DRAW WATER BOX ---------------- */
    private void trapEntityInWaterBox(LivingEntity target) {
        // Slow them down completely
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 10)); // 3s freeze
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 250)); // No jump

        new BukkitRunnable() {
            int duration = 60; // 3 seconds (20 ticks * 3)
            
            @Override
            public void run() {
                if (duration <= 0 || target.isDead()) {
                    this.cancel();
                    return;
                }

                Location loc = target.getLocation();
                // Draw a 2x2x2 Box of particles
                for (double x = -1; x <= 1; x += 0.5) {
                    for (double y = 0; y <= 2.5; y += 0.5) {
                        for (double z = -1; z <= 1; z += 0.5) {
                            // Only draw borders (hollow box)
                            boolean isEdge = (Math.abs(x) >= 1 || Math.abs(z) >= 1 || y == 0 || y >= 2.5);
                            if (isEdge) {
                                target.getWorld().spawnParticle(Particle.DRIP_WATER, loc.clone().add(x, y, z), 1, 0, 0, 0, 0);
                                target.getWorld().spawnParticle(Particle.FALLING_WATER, loc.clone().add(x, y, z), 1, 0, 0, 0, 0);
                            }
                        }
                    }
                }
                duration -= 5;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 5L);
    }

    // Cooldown Logic (Manager Connected)
    private boolean isCool(Player p, String action) {
    // Purana 'seconds' wala logic hata do, manager config se khud seconds uthayega
    if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), action)) {
        long remaining = SpcialSmp.get().getCooldownManager().getRemainingSeconds(p, getName(), action);
        p.sendMessage("§cWait " + remaining + "s");
        return false;
    }

    // Cooldown apply manager ke through karo
    SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), action);
    return true;
    }
}
