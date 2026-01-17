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
import org.bukkit.util.Vector;

import java.util.*;

public class UltimateCard extends BaseCard {

    // Orbiting stands track karne ke liye
    private final Map<UUID, List<ArmorStand>> orbiting = new HashMap<>();

    @Override
    public String getName() {
        return "Ultimate Card";
    }

    /* ================= LEFT CLICK: LIGHTNING BEAM ================= */
    @Override
    public void leftClick(Player p) {
        // Speed Effect
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 2, false, false));

        // 20 Seconds duration
        long end = System.currentTimeMillis() + (20_000);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isOnline() || System.currentTimeMillis() > end) {
                    this.cancel();
                    return;
                }

                // Raytrace 50 blocks
                RayTraceResult r = p.getWorld().rayTraceBlocks(
                        p.getEyeLocation(),
                        p.getEyeLocation().getDirection(),
                        50
                );

                if (r != null && r.getHitPosition() != null) {
                    Location hit = r.getHitPosition().toLocation(p.getWorld());
                    
                    // Lightning Effect (Damage wala nahi, sirf visual)
                    p.getWorld().strikeLightningEffect(hit);

                    // Custom Damage Area
                    p.getWorld().getNearbyEntities(hit, 4, 4, 4).forEach(e -> {
                        if (e instanceof LivingEntity le && !le.equals(p)) {
                            le.damage(20.0, p); // Instant high damage
                        }
                    });
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L); // Har second strike karega
    }

    /* ================= RIGHT CLICK: TACTICAL NUKE ================= */
    @Override
    public void rightClick(Player p) {
        // ... (Iska code same rahega ya aap chaho to isme bhi Raytrace daal sakte ho)
        // Abhi ke liye Shift+Right aur Orbit par focus hai.
    }

    /* ================= SHIFT + RIGHT: GIANT SWORD DROP (FIXED) ================= */
    @Override
    public void shiftRightClick(Player p) {

        // 1. AIMING: Jahan player dekh raha hai wahan girega (Max 80 blocks door)
        RayTraceResult ray = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(), 
                p.getEyeLocation().getDirection(), 
                80
        );

        Location targetBlock;
        if (ray != null && ray.getHitPosition() != null) {
            targetBlock = ray.getHitPosition().toLocation(p.getWorld());
        } else {
            // Agar hawa mein dekh raha hai to 20 block door gira do
            targetBlock = p.getLocation().add(p.getEyeLocation().getDirection().multiply(20));
            targetBlock.setY(p.getWorld().getHighestBlockYAt(targetBlock)); // Zameen par
        }

        // Spawn Location (Hawa mein 40 blocks upar)
        Location spawnLoc = targetBlock.clone().add(0, 40, 0);

        // 2. GIANT SWORD CREATION (Using ArmorStand Scale in 1.21)
        ArmorStand stand = p.getWorld().spawn(spawnLoc, ArmorStand.class);
        stand.setInvisible(true);
        stand.setGravity(false); // Hum khud move karenge
        stand.setMarker(true);   // Hitbox hatane ke liye
        
        // IMPORTANT: 1.21 Scale Attribute (Isse sword Giant banegi)
        if (stand.getAttribute(Attribute.GENERIC_SCALE) != null) {
            stand.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(6.0); // 6x Bada size
        }

        // Sword haath mein dena
        stand.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

        // 3. POSE FIX: Handle Upar, Tip Niche (Sword seedhi giregi)
        // X = 180 degrees (Math.PI) se haath neeche ghum jayega
        stand.setRightArmPose(new EulerAngle(Math.PI / 1.1, 0, 0));

        // 4. FALLING ANIMATION LOOP
        new BukkitRunnable() {
            @Override
            public void run() {
                if (stand.isDead()) {
                    this.cancel();
                    return;
                }

                // Niche move karo (Fast speed)
                Location current = stand.getLocation().add(0, -2.5, 0); // Speed 2.5 block/tick
                stand.teleport(current);

                // 5. IMPACT CHECK
                // Jab sword zameen ke kareeb ho (Target Y + thoda offset taki sword ghusi hui lage)
                if (current.getY() <= targetBlock.getY() - 2) {
                    
                    // Explosion & Sound
                    p.getWorld().playSound(current, Sound.ENTITY_GENERIC_EXPLODE, 10f, 0.5f);
                    p.getWorld().playSound(current, Sound.BLOCK_ANVIL_LAND, 5f, 0.1f);
                    
                    // PARTICLE RING
                    p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, current, 10);
                    
                    // 6. BIG EXPLOSION (20 TNT Equivalent)
                    // Power 12F - 15F kaafi hoti hai map udane ke liye.
                    // breakBlocks = true (Map tod dega)
                    p.getWorld().createExplosion(current, 12.0F, true, true, p);

                    // Damage nearby
                    for (Entity e : p.getWorld().getNearbyEntities(current, 10, 10, 10)) {
                        if (e instanceof LivingEntity le && !le.equals(p)) {
                            le.damage(100.0, p); // Instant Kill mostly
                        }
                    }

                    // Sword ko remove karo 5 second baad
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            stand.remove();
                        }
                    }.runTaskLater(SpcialSmp.get(), 100L); // 5 Seconds stay

                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ================= ORBIT EFFECT (PERFECT CIRCLE 3D) ================= */
    public void startOrbit(Player p) {

        if (orbiting.containsKey(p.getUniqueId())) return;

        List<ArmorStand> stands = new ArrayList<>();
        orbiting.put(p.getUniqueId(), stands);

        // 8 Stars spawn karenge
        for (int i = 0; i < 8; i++) {
            ArmorStand as = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
            as.setInvisible(true);
            as.setMarker(true);
            as.setGravity(false);
            as.setSmall(true); // Chota stand = Item close to center
            
            as.getEquipment().setItemInMainHand(new ItemStack(Material.NETHER_STAR));
            
            // FIX: Arm Pose taki Star "Khada" dikhe (Vertical), "Leta hua" (Flat) nahi
            as.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
            
            stands.add(as);
        }

        new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                // Check conditions
                if (!p.isOnline() || !isHoldingCard(p)) {
                    stands.forEach(Entity::remove);
                    orbiting.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }

                angle += 0.15; // Speed of rotation
                double radius = 2.5; // Player se doori

                for (int i = 0; i < stands.size(); i++) {
                    double offset = (2 * Math.PI / stands.size()) * i;
                    double currAngle = angle + offset;

                    // FIX: Pure Horizontal Circle (No Y waving)
                    double x = radius * Math.cos(currAngle);
                    double z = radius * Math.sin(currAngle);
                    
                    // Location set karo
                    Location loc = p.getLocation().clone().add(x, 1.2, z); // Chest height

                    // FIX: Star ko player ki taraf face karwana (Look At Player)
                    // Isse Star 3D dikhega aur ghumta hua lagega
                    Location lookDir = p.getLocation().clone().subtract(loc);
                    loc.setDirection(lookDir.toVector());

                    stands.get(i).teleport(loc);
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    // Helper to check card holding
    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return false;
        return getName().equals(item.getItemMeta().getDisplayName()); // Ya NBT check
    }
                                }
