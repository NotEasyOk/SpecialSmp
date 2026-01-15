package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class EndermanCard implements Card {

    @Override
    public String getName() {
        return "Enderman Card";
    }

    /* ---------------- LEFT CLICK ---------------- */
    // Aim direction teleport (Enderman style)
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
        }
    }

    /* ---------------- RIGHT CLICK ---------------- */
    // Pull aimed player to you
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
    }

    /* ---------------- SHIFT + RIGHT CLICK ---------------- */
    // Dragon Breath area for 10s
    @Override
    public void shiftRightClick(Player p) {

        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                40
        );

        Location loc = (r != null && r.getHitPosition() != null)
                ? r.getHitPosition().toLocation(p.getWorld()).add(0, 0.1, 0)
                : p.getLocation();

        AreaEffectCloud cloud = p.getWorld().spawn(loc, AreaEffectCloud.class);
        cloud.setRadius(4.5f);
        cloud.setDuration(200); // 10 seconds
        cloud.setWaitTime(0);
        cloud.setParticle(org.bukkit.Particle.DRAGON_BREATH);
        cloud.addCustomEffect(
                new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 40, 1),
                true
        );
        cloud.setSource(p);
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
