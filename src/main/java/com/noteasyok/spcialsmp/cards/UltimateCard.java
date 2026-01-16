package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;
import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Giant; // Added for Big Sword
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.Location;

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

            if (r != null && r.getHitPosition() != null) {
                Location hit = r.getHitPosition().toLocation(p.getWorld());
                p.getWorld().strikeLightningEffect(hit);

                p.getWorld().getNearbyEntities(hit, 4, 4, 4).forEach(e -> {
                    if (e instanceof LivingEntity le && !le.equals(p)) {
                        le.damage(20.0, p);
                    }
                });
            }
        }, 0L, 20L);
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

        // FIX: Item drop ki jagah ArmorStand use kiya taki koi utha na sake
        ArmorStand sword = p.getWorld().spawn(spawn, ArmorStand.class);
        sword.setInvisible(true);
        sword.setMarker(true);
        sword.setGravity(true); // Gravity on taki neeche gire
        sword.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

        // Cleanup task (Security: Agar kahin atak gayi to remove ho jaye)
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), sword::remove, 60L);

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

    /* ================= SHIFT + RIGHT (BIG SWORD) ================= */
    @Override
    public void shiftRightClick(Player p) {

        Location start = p.getEyeLocation();
        Vector dir = start.getDirection().normalize();

        Location spawn = start.clone().add(dir.multiply(12)).add(0, 25, 0);

        // FIX: Giant use kiya taki sword "Bahut Badi" dikhe
        Giant giant = p.getWorld().spawn(spawn, Giant.class);
        giant.setInvisible(false); // Potion se invisible karenge
        giant.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, false, false));
        giant.setAI(false); // Move nahi karega
        giant.setGravity(false); // Custom gravity use karenge
        giant.setInvulnerable(true);

        giant.getEquipment().setItemInMainHand(
                new ItemStack(Material.DIAMOND_SWORD)
        );

        // DROP ANIMATION LOOP
        Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), new Runnable() {
            int ticks = 0;
            boolean landed = false;

            @Override
            public void run() {

                // Agar 5 second (100 ticks) se jyada ho gaya to remove karo
                if (ticks > 140 || giant.isDead()) { 
                    giant.remove();
                    return;
                }

                if (!landed) {
                    // FALLING LOGIC
                    Location l = giant.getLocation().add(0, -1.5, 0); // Fast fall
                    giant.teleport(l);

                    // HIT GROUND CHECK
                    // Giant ka pair block check karega
                    if (l.getBlock().getType().isSolid()) {
                        landed = true;
                        
                        // FIX: Sword ko "Adha Gusana" (Embed in ground)
                        // Giant bahut bada hota hai, isliye usko zameen ke andar adjust karna padega
                        Location stuckLoc = l.clone().add(0, -3.5, 0); 
                        giant.teleport(stuckLoc);

                        // EFFECT
                        giant.getWorld().createExplosion(l, 8f, false, false, p);
                        giant.getWorld().playSound(l, Sound.ENTITY_GENERIC_EXPLODE, 4f, 1f);
                        giant.getWorld().playSound(l, Sound.BLOCK_ANVIL_LAND, 2f, 0.5f);

                        // DAMAGE
                        for (Entity e : giant.getWorld().getNearbyEntities(l, 6, 6, 6)) {
                            if (e instanceof LivingEntity le && le != p) {
                                le.damage(45, p);
                            }
                        }

                        // FIX: 5 Seconds baad gayab (Despawn)
                        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
                            giant.remove();
                        }, 100L); // 100 Ticks = 5 Seconds
                        
                        // Loop yahi stop kar do (cancel this runnable inside wrapper logic if needed, 
                        // but here we just stop processing movement)
                        return; 
                    }
                }
                
                // Agar land ho chuka hai to loop bas timer ke liye chalega (ticks count karega for safety)
                if (landed) {
                     // Do nothing, just wait for the cleanup task defined above
                }

                ticks++;
            }
        }, 0L, 1L);
    }

    /* ================= ORBIT EFFECT (3D FIX) ================= */
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
            as.getEquipment().setItemInMainHand(
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

                    double radius = 2.0;
                    double step = (2 * Math.PI) / stands.size();
                    double time = System.currentTimeMillis() / 400.0; // Speed

                    for (int i = 0; i < stands.size(); i++) {
                        double angle = time + (i * step);
                        
                        // FIX: 3D Orbit Calculation (Tilted Ring)
                        // Y axis mein bhi change laya gaya hai (Math.sin(angle))
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        double y = Math.sin(angle) * 0.8 + 1.2; // Wavy/Tilted effect

                        Location loc = p.getLocation().add(x, y, z);
                        
                        // Rotation taki star player ki taraf face kare
                        loc.setYaw(p.getLocation().getYaw());
                        
                        stands.get(i).teleport(loc);
                    }

                }, 0L, 2L
        );
    }
                                                                     }
