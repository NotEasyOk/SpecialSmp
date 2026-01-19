package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EndermanCard extends BaseCard {

    // Cooldown track karne ke liye Map
    private final Map<UUID, Long> dragonBreathCooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Enderman Card";
    }

    /* ---------------- LEFT CLICK ---------------- */
    @Override
    public void leftClick(Player p) {
        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                60
        );

        Location base = (r != null && r.getHitPosition() != null)
                ? r.getHitPosition().toLocation(p.getWorld())
                : p.getLocation().add(p.getLocation().getDirection().multiply(10));

        Location safe = findSafeLocation(base, p.getWorld());
        if (safe != null) {
            p.teleport(safe);
            p.getWorld().spawnParticle(
                    org.bukkit.Particle.PORTAL,
                    safe,
                    80,
                    0.5, 1, 0.5,
                    0.2
            );
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        }
    }

    /* ---------------- RIGHT CLICK ---------------- */
    @Override
    public void rightClick(Player p) {
        RayTraceResult r = p.getWorld().rayTraceEntities(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                40,
                e -> e instanceof Player && !e.equals(p)
        );

        if (r == null) return;

        Entity e = r.getHitEntity();
        if (!(e instanceof Player target)) return;

        target.teleport(p.getLocation());
        target.getWorld().spawnParticle(
                org.bukkit.Particle.PORTAL,
                target.getLocation(),
                60,
                0.5, 1, 0.5,
                0.2
        );
        p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
    }

    /* ---------------- SHIFT + RIGHT CLICK (FIXED) ---------------- */
    // Dragon Breath: 10s Duration, High Damage, Cooldown Logic
    @Override
    public void shiftRightClick(Player p) {
        
        // 1. COOLDOWN CHECK
        UUID id = p.getUniqueId();
        if (dragonBreathCooldowns.containsKey(id)) {
            long timeEnds = dragonBreathCooldowns.get(id);
            if (System.currentTimeMillis() < timeEnds) {
                long timeLeft = (timeEnds - System.currentTimeMillis()) / 1000;
                p.sendMessage(ChatColor.RED + "Dragon Breath cooling down: " + timeLeft + "s");
                return;
            }
        }

        // 2. Raytrace to find ground
        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                40
        );

        Location loc = (r != null && r.getHitPosition() != null)
                ? r.getHitPosition().toLocation(p.getWorld()).add(0, 0.1, 0)
                : p.getLocation();

        // 3. Spawn AreaEffectCloud
        AreaEffectCloud cloud = p.getWorld().spawn(loc, AreaEffectCloud.class);
        cloud.setRadius(4.5f);
        cloud.setDuration(200); // 10 seconds (20 ticks * 10)
        
        // IMPORTANT FIXES FOR DAMAGE:
        cloud.setWaitTime(0);             // Turant start hoga
        cloud.setReapplicationDelay(20);  // Har 1 second (20 ticks) mein damage dega
        cloud.setRadiusOnUse(0.0f);       // Player ko touch karne par cloud gayab nahi hoga
        cloud.setRadiusPerTick(0.0f);     // Cloud ka size kam nahi hoga
        
        cloud.setParticle(org.bukkit.Particle.DRAGON_BREATH);
        
        // Adding Instant Damage (Harming II)
        // Duration 1 tick rakha hai kyunki cloud bar-bar apply karega
        cloud.addCustomEffect(
                new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), 
                false
        );
        
        cloud.setSource(p); // Taaki kill credit player ko mile

        // 4. Set Cooldown (10 Seconds)
        dragonBreathCooldowns.put(id, System.currentTimeMillis() + 10000);
        p.sendMessage(ChatColor.DARK_PURPLE + "Dragon Breath Released!");
    }

    /* ---------------- SAFE TELEPORT HELPER ---------------- */
    private Location findSafeLocation(Location base, World w) {
        for (int i = 0; i < 12; i++) {
            double x = base.getX() + (Math.random() * 6 - 3);
            double z = base.getZ() + (Math.random() * 6 - 3);
            int y = w.getHighestBlockYAt((int) x, (int) z) + 1;

            Location loc = new Location(w, x, y, z);
            if (loc.getBlock().isPassable() && loc.clone().add(0, 1, 0).getBlock().isPassable()) {
                return loc;
            }
        }
        return null;
    }
            }
