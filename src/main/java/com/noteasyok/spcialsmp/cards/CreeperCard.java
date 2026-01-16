package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;

public class CreeperCard implements Card {

    @Override
    public String getName() {
        return "Creeper Card";
    }

    /* ================= LEFT CLICK =================
       Big explosion at target block
     */
    @Override
    public void leftClick(Player p) {
        Location loc = p.getTargetBlockExact(12) != null
                ? p.getTargetBlockExact(12).getLocation().add(0, 1, 0)
                : p.getLocation();

        p.getWorld().createExplosion(loc, 5f, true, true, p);
    }

    /* ================= RIGHT CLICK =================
       Orbital strike (ground touch explosion)
     */
    @Override
    public void rightClick(Player p) {

        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                120
        );

        if (r == null || r.getHitPosition() == null) return;

        World w = p.getWorld();
        Location hit = r.getHitPosition().toLocation(w);
        Location spawn = hit.clone().add(0, 35, 0);

        TNTPrimed tnt = w.spawn(spawn, TNTPrimed.class);
        tnt.setVelocity(new Vector(0, -2.5, 0));
        tnt.setFuseTicks(200);
        tnt.setYield(10f); // ~10 TNT power
        tnt.setIsIncendiary(false);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!tnt.isValid()) {
                    cancel();
                    return;
                }
                if (tnt.isOnGround()) {
                    Location l = tnt.getLocation();
                    tnt.remove();
                    w.createExplosion(l, 10f, true, true, p);
                    cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ================= SHIFT + RIGHT CLICK =================
       TNT Rain for 5 seconds
     */
    @Override
    public void shiftRightClick(Player p) {
TT
        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                120
        );

        if (r == null || r.getHitPosition() == null) return;

        World w = p.getWorld();
        Location center = r.getHitPosition().toLocation(w);

        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {

                if (ticks >= 100) { // 5 seconds
                    cancel();
                    return;
                }

                Location spawn = center.clone().add(
                        (Math.random() * 8) - 4,
                        30,
                        (Math.random() * 8) - 4
                );

                TNTPrimed tnt = w.spawn(spawn, TNTPrimed.class);
                tnt.setVelocity(new Vector(0, -2.5, 0));
                tnt.setFuseTicks(200);
                tnt.setYield(6f);
                tnt.setIsIncendiary(false);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!tnt.isValid()) {
                            cancel();
                            return;
                        }
                        if (tnt.isOnGround()) {
                            Location l = tnt.getLocation();
                            tnt.remove();
                            w.createExplosion(l, 6f, true, true, p);
                            cancel();
                        }
                    }
                }.runTaskTimer(SpcialSmp.get(), 0L, 1L);

                ticks += 10;
            }

        }.runTaskTimer(SpcialSmp.get(), 0L, 10L);
    }
}
