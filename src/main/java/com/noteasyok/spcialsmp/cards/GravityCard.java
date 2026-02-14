package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CooldownManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GravityCard extends BaseCard {

    // 1. Pattern Fix: Constructor following your super class
    public GravityCard() {
        super("Gravity Card", Material.ECHO_SHARD, 
                "§7Control the fundamental forces.",
                " ",
                "§6§lABILITY 1: §eGravity Push §7(Left Click)",
                "§fAim at an enemy to launch them.",
                " ",
                "§6§lABILITY 2: §5Black Hole §7(Right Click)",
                "§fSummon a vortex that pulls enemies.",
                " ",
                "§6§lULTIMATE: §dZero-G Zone §7(Shift + Right)",
                "§fCreate a zone where gravity fails.",
                " ",
                "§c§l(!) §7Owner is immune to effects.");
    }

    // 2. Pattern Fix: Added Missing getName()
    @Override
    public String getName() {
        return "Gravity Card";
    }

    // 3. Pattern Fix: Added Missing getMaterial()
    @Override
    public Material getMaterial() {
        return Material.ECHO_SHARD;
    }

    // 4. Pattern Fix: Added Custom Model Data support
    @Override
    public ItemStack getItemStackWithLore(String name) {
        ItemStack item = super.getItemStackWithLore(name);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Yahan 105 ki jagah apna texture ID dalo
            meta.setCustomModelData(105); 
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action a = e.getAction();

        if (a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK) {
            if (CooldownManager.checkCooldown(p, getName() + " Left")) {
                performGravityPush(p);
                CooldownManager.setCooldown(p, getName() + " Left", 3);
            }
        } 
        else if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
            if (p.isSneaking()) {
                if (CooldownManager.checkCooldown(p, getName() + " Shift")) {
                    performZeroGravityZone(p);
                    CooldownManager.setCooldown(p, getName() + " Shift", 20);
                }
            } else {
                if (CooldownManager.checkCooldown(p, getName() + " Right")) {
                    performBlackHole(p);
                    CooldownManager.setCooldown(p, getName() + " Right", 10);
                }
            }
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
            p.sendMessage("§5§lGRAVITY » §fYeeted §d" + victim.getName() + "§f!");
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

                double radius = 3.5;
                double x = radius * Math.cos(ticks * 0.2);
                double z = radius * Math.sin(ticks * 0.2);
                targetLoc.getWorld().spawnParticle(Particle.WITCH, targetLoc.clone().add(x, 0, z), 1);
                targetLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, targetLoc, 2);

                for (Entity e : targetLoc.getWorld().getNearbyEntities(targetLoc, 6, 6, 6)) {
                    if (e instanceof LivingEntity && !e.equals(p)) {
                        Vector pull = targetLoc.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.4);
                        pull.setY(0.3);
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
