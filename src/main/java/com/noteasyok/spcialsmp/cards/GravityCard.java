package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CooldownManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.List;

public class GravityCard extends BaseCard {

    public GravityCard() {
        super("§5§lGravity Card", Material.ECHO_SHARD, 
            "§8§m--------------------------",
            "§7Control the fundamental forces",
            " ",
            "§6§l⚡ §e§lLEFT CLICK: §bGravity Push",
            "§f  ➥ §7Launch enemies into the sky.",
            " ",
            "§5§l🌀 §d§lRIGHT CLICK: §5Black Hole",
            "§f  ➥ §7Pulls and spins nearby mobs.",
            " ",
            "§b§l✨ §b§lSHIFT+R: §3Zero-G Zone",
            "§f  ➥ §7Floating zone for 8 seconds.",
            "§8§m--------------------------",
            "§e§lRarity: §d§lMYTHIC"
        );
    }

    @Override
    public String getName() {
        return "Gravity Card";
    }

    @Override
    public Material getMaterial() {
        return Material.ECHO_SHARD;
    }

    @Override
    public int getModelData() {
        return 105;
    }

    // ABILITY 1: Left Click (Gravity Push)
    @Override
    public void leftClick(Player p) {
        // Aapke system ke mutabiq String cooldown "3"
        if (CooldownManager.checkCooldown(p, getName() + " Left", "3")) {
            performGravityPush(p);
            CooldownManager.setCooldown(p, getName() + " Left", "3");
        }
    }

    // ABILITY 2: Right Click (Black Hole)
    @Override
    public void rightClick(Player p) {
        if (CooldownManager.checkCooldown(p, getName() + " Right", "10")) {
            performBlackHole(p);
            CooldownManager.setCooldown(p, getName() + " Right", "10");
        }
    }

    // ABILITY 3: Shift + Right Click (Zero-G Zone)
    @Override
    public void shiftRightClick(Player p) {
        if (CooldownManager.checkCooldown(p, getName() + " Shift", "20")) {
            performZeroGravityZone(p);
            CooldownManager.setCooldown(p, getName() + " Shift", "20");
        }
    }

    // --- PHYSICS LOGIC ---

    private void performGravityPush(Player p) {
        Entity target = getTargetEntity(p, 20);
        if (target instanceof LivingEntity victim && !target.equals(p)) {
            p.getWorld().spawnParticle(Particle.SONIC_BOOM, p.getEyeLocation().add(p.getLocation().getDirection()), 1);
            p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 1.5f);

            Vector dir = p.getLocation().getDirection().normalize();
            victim.setVelocity(dir.multiply(2.5).setY(1.2));
            p.sendMessage("§5§lGRAVITY » §fYeeted §d" + victim.getName());
        } else {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
        }
    }

    private void performBlackHole(Player p) {
        Location targetLoc = p.getTargetBlock(null, 25).getLocation().add(0, 2, 0);
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);
        p.sendMessage("§5§lGRAVITY » §fBlack Hole opened!");

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) { this.cancel(); return; }

                double x = 3.5 * Math.cos(ticks * 0.2);
                double z = 3.5 * Math.sin(ticks * 0.2);
                targetLoc.getWorld().spawnParticle(Particle.WITCH, targetLoc.clone().add(x, 0, z), 1);
                targetLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, targetLoc, 2);

                for (Entity e : targetLoc.getWorld().getNearbyEntities(targetLoc, 6, 6, 6)) {
                    if (e instanceof LivingEntity && !e.equals(p)) {
                        Vector pull = targetLoc.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.4).setY(0.3);
                        e.setVelocity(pull);
                    }
                }
                ticks++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    private void performZeroGravityZone(Player p) {
        Location center = p.getLocation();
        p.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1f, 0.8f);

        new BukkitRunnable() {
            int duration = 0;
            @Override
            public void run() {
                if (duration >= 160) {
                    for (Entity e : center.getWorld().getNearbyEntities(center, 6, 6, 6)) {
                        if (e instanceof LivingEntity le) le.setGravity(true);
                    }
                    this.cancel();
                    return;
                }

                center.getWorld().spawnParticle(Particle.DRAGON_BREATH, center, 10, 5, 0.5, 5, 0.02);
                for (Entity e : center.getWorld().getNearbyEntities(center, 5, 5, 5)) {
                    if (e instanceof LivingEntity le && !e.equals(p)) {
                        le.setGravity(false);
                        le.setVelocity(new Vector(0, 0.05, 0));
                    }
                }
                duration++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    private Entity getTargetEntity(Player p, int range) {
        List<Entity> nearby = p.getNearbyEntities(range, range, range);
        Entity target = null;
        double maxDot = 0.95;
        for (Entity e : nearby) {
            if (e instanceof LivingEntity && !e.equals(p)) {
                Vector toEntity = e.getLocation().toVector().subtract(p.getEyeLocation().toVector()).normalize();
                double dot = p.getEyeLocation().getDirection().dot(toEntity);
                if (dot > maxDot) { maxDot = dot; target = e; }
            }
        }
        return target;
    }
            }
