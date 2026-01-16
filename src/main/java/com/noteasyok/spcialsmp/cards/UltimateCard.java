package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;
import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.location;

import java.util.*;

public class UltimateCard extends BaseCard {

    private final Map<UUID, List<ArmorStand>> orbiting = new HashMap<>();

    @Override
    public String getName() {
        return "Ultimate Card";
    }

    /* ================= LEFT CLICK ================= */
    @Override
    public void leftClick(Player p) {

        p.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, 20 * 20, 2, false, false
        ));

        long end = System.currentTimeMillis() + (20_000);

        Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), task -> {

            if (!p.isOnline() || System.currentTimeMillis() > end) {
                task.cancel();
                return;
            }

            RayTraceResult r = p.getWorld().rayTraceBlocks(
                    p.getEyeLocation(),
                    p.getEyeLocation().getDirection(),
                    50
            );

            Location hit = null;

if (r != null && r.getHitPosition() != null) {
    hit = r.getHitPosition().toLocation(p.getWorld());
    p.getWorld().strikeLightningEffect(hit);
}

if (hit != null) {
    p.getWorld().getNearbyEntities(hit, 4, 4, 4).forEach(e -> {
        if (e instanceof LivingEntity le && le != p) {
            le.damage(20.0, p); // 10 hearts damage
        }
    });
}

    /* ================= RIGHT CLICK ================= */
    @Override
    public void rightClick(Player p) {

        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                80
        );
        if (r == null || r.getHitPosition() == null) return;

        Location hit = r.getHitPosition().toLocation(p.getWorld());
        
        Location spawn = hit.clone().add(0, 40, 0);

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        p.getWorld().dropItem(spawn, sword)
                .setVelocity(new Vector(0, -2.2, 0));

        p.getWorld().spawnParticle(
                Particle.EXPLOSION,
                hit, 8, 1.5, 1.5, 1.5
        );

        p.getWorld().playSound(
                hit, Sound.ENTITY_GENERIC_EXPLODE, 3f, 0.6f
        );

        p.getNearbyEntities(7, 7, 7).forEach(e -> {
            if (e instanceof Player target && !target.equals(p)) {
                target.damage(30, p);
            }
        });
    }

    /* ================= SHIFT + RIGHT ================= */
    @Override
public void shiftRightClick(Player p) {

    Location start = p.getEyeLocation();
    Vector dir = start.getDirection().normalize();

    Location spawn = start.clone().add(dir.multiply(12)).add(0, 20, 0);

    ArmorStand sword = p.getWorld().spawn(spawn, ArmorStand.class);
    sword.setInvisible(true);
    sword.setMarker(true);
    sword.setGravity(false);
    sword.setInvulnerable(true);

    sword.getEquipment().setItemInMainHand(
            new ItemStack(Material.DIAMOND_SWORD)
    );

    // DROP ANIMATION
    Bukkit.getScheduler().runTaskTimer(
            SpcialSmp.get(),
            new Runnable() {
                int ticks = 0;

                @Override
                public void run() {

                    if (ticks > 100 || sword.isDead()) {
                        sword.remove();
                        return;
                    }

                    Location l = sword.getLocation().add(0, -0.6, 0);
                    sword.teleport(l);

                    // DAMAGE + AREA DAMAGE
                    for (Entity e : sword.getWorld().getNearbyEntities(l, 5, 6, 5)) {
                        if (e instanceof LivingEntity le && le != p) {
                            le.damage(35, p);
                        }
                    }

                    // HIT GROUND
                    if (l.getBlock().getType().isSolid()) {

                        sword.getWorld().createExplosion(
                                l,
                                8f,
                                false,
                                false,
                                p
                        );

                        sword.remove();
                        return;
                    }

                    ticks++;
                }
            },
            0L,
            1L
    );
}

    /* ================= ORBIT EFFECT ================= */
    public void startOrbit(Player p) {

        if (orbiting.containsKey(p.getUniqueId())) return;

        List<ArmorStand> stands = new ArrayList<>();
        orbiting.put(p.getUniqueId(), stands);

        for (int i = 0; i < 9; i++) {
            ArmorStand as = p.getWorld().spawn(
                    p.getLocation(), ArmorStand.class
            );
            as.setInvisible(true);
            as.setMarker(true);
            as.setSmall(true);
            as.setItem(EquipmentSlot.HEAD,
                    new ItemStack(Material.NETHER_STAR));
            stands.add(as);
        }

        Bukkit.getScheduler().runTaskTimer(
                SpcialSmp.get(),
                task -> {

                    if (!p.isOnline()
                            || !p.getInventory().getItemInMainHand().hasItemMeta()
                            || !getName().equals(
                            p.getInventory()
                                    .getItemInMainHand()
                                    .getItemMeta()
                                    .getDisplayName()
                    )) {
                        stands.forEach(Entity::remove);
                        orbiting.remove(p.getUniqueId());
                        task.cancel();
                        return;
                    }

                    double radius = 1.8;
                    double step = (2 * Math.PI) / stands.size();
                    double time = System.currentTimeMillis() / 400.0;

                    for (int i = 0; i < stands.size(); i++) {
                        double angle = time + (i * step);
                        Location loc = p.getLocation().add(
                                Math.cos(angle) * radius,
                                1.2,
                                Math.sin(angle) * radius
                        );
                        stands.get(i).teleport(loc);
                    }

                }, 0L, 2L
        );
    }
}
