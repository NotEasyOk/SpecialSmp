package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class UltimateCard extends BaseCard {

    private final Map<UUID, List<ArmorStand>> orbiting = new HashMap<>();

    @Override
    public String getName() {
        return "Ultimate Card";
    }

    /* ================= LEFT CLICK: LIGHTNING BEAM ================= */
    @Override
    public void leftClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 2, false, false));
        p.sendMessage("§6§lULTIMATE: §eLightning Beam Activated!");

        long end = System.currentTimeMillis() + (20_000);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isOnline() || System.currentTimeMillis() > end || !isHoldingCard(p)) {
                    this.cancel();
                    return;
                }

                RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 50);

                if (r != null && r.getHitPosition() != null) {
                    Location hit = r.getHitPosition().toLocation(p.getWorld());
                    p.getWorld().strikeLightningEffect(hit);

                    p.getWorld().getNearbyEntities(hit, 4, 4, 4).forEach(e -> {
                        if (e instanceof LivingEntity le && !le.equals(p)) {
                            le.damage(20.0, p);
                        }
                    });
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    /* ================= RIGHT CLICK: TRIGGER ORBIT ================= */
    @Override
    public void rightClick(Player p) {
        if (!orbiting.containsKey(p.getUniqueId())) {
            startOrbit(p);
            p.sendMessage("§6§lULTIMATE: §fNether Stars Orbiting...");
        } else {
            p.sendMessage("§cOrbit is already active!");
        }
    }

    /* ================= SHIFT + RIGHT: GIANT SWORD DROP ================= */
    @Override
    public void shiftRightClick(Player p) {
        RayTraceResult ray = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 80);

        Location targetBlock;
        if (ray != null && ray.getHitPosition() != null) {
            targetBlock = ray.getHitPosition().toLocation(p.getWorld());
        } else {
            targetBlock = p.getLocation().add(p.getEyeLocation().getDirection().multiply(20));
            targetBlock.setY(p.getWorld().getHighestBlockYAt(targetBlock));
        }

        Location spawnLoc = targetBlock.clone().add(0, 40, 0);
        ArmorStand stand = p.getWorld().spawn(spawnLoc, ArmorStand.class);
        stand.setInvisible(true);
        stand.setGravity(false);
        stand.setMarker(true);
        
        if (stand.getAttribute(Attribute.GENERIC_SCALE) != null) {
            stand.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(6.0);
        }

        stand.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        // Sword point downwards
        stand.setRightArmPose(new EulerAngle(Math.toRadians(180), 0, 0));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (stand.isDead()) {
                    this.cancel();
                    return;
                }

                Location current = stand.getLocation().add(0, -2.5, 0);
                stand.teleport(current);

                if (current.getY() <= targetBlock.getY() - 1) {
                    p.getWorld().playSound(current, Sound.ENTITY_GENERIC_EXPLODE, 10f, 0.5f);
                    p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, current, 5);
                    p.getWorld().createExplosion(current, 12.0F, true, true, p);

                    stand.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ================= ORBIT EFFECT (NETHER STARS) ================= */
    public void startOrbit(Player p) {
        List<ArmorStand> stands = new ArrayList<>();
        int count = 8;

        for (int i = 0; i < count; i++) {
            ArmorStand as = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
            as.setInvisible(true);
            as.setMarker(true);
            as.setGravity(false);
            as.setSmall(true);
            as.getEquipment().setItemInMainHand(new ItemStack(Material.NETHER_STAR));
            // Rotate arm to make the star face outwards
            as.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
            stands.add(as);
        }
        orbiting.put(p.getUniqueId(), stands);

        new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (!p.isOnline() || !isHoldingCard(p)) {
                    stands.forEach(Entity::remove);
                    orbiting.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }

                angle += 0.15;
                double radius = 2.5;

                for (int i = 0; i < stands.size(); i++) {
                    double offset = (2 * Math.PI / stands.size()) * i;
                    double currAngle = angle + offset;

                    double x = radius * Math.cos(currAngle);
                    double z = radius * Math.sin(currAngle);
                    
                    Location loc = p.getLocation().clone().add(x, 1.2, z);
                    
                    // Make ArmorStand look at player center
                    Vector direction = p.getLocation().toVector().subtract(loc.toVector());
                    loc.setDirection(direction);

                    stands.get(i).teleport(loc);
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return false;
        
        // ✅ NBT Check for reliability
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        String id = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return getName().equals(id);
    }
            }
