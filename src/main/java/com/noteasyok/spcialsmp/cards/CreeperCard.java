package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class CreeperCard implements Card, Listener {

    @Override
    public String getName() {
        return "Creeper Card";
    }

    /* ---------------- LEFT CLICK ---------------- */
    @Override
    public void leftClick(Player p) {
        Location loc = p.getTargetBlockExact(12) != null
                ? p.getTargetBlockExact(12).getLocation().add(0, 1, 0)
                : p.getLocation();

        p.getWorld().createExplosion(loc, 5.0f, true, true, p);
    }

    /* ---------------- RIGHT CLICK : ORBITAL STRIKE ---------------- */
    @Override
    public void rightClick(Player p) {

        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                120
        );

        if (r == null || r.getHitPosition() == null) return;

        Location hit = r.getHitPosition().toLocation(p.getWorld());
        Location spawn = hit.clone().add(0, 35, 0);

        World w = p.getWorld();
        TNTPrimed tnt = w.spawn(spawn, TNTPrimed.class);

        tnt.setVelocity(new Vector(0, -2.5, 0));
        tnt.setFuseTicks(200); // long fuse, manual blast
        tnt.setYield(10f);     // ~10 TNT power
        tnt.setIsIncendiary(false);

        // Ground touch detector
        Bukkit.getScheduler().runTaskTimer(
                Bukkit.getPluginManager().getPlugin("SpecialSmp"),
                task -> {
                    if (tnt.isDead() || !tnt.isValid()) {
                        task.cancel();
                        return;
                    }

                    if (tnt.isOnGround()) {
                        Location l = tnt.getLocation();
                        tnt.remove();
                        w.createExplosion(l, 10f, true, true, p);
                        task.cancel();
                    }
                },
                0L, 1L
        );
    }

    /* ---------------- SHIFT + RIGHT : TNT RAIN ---------------- */
    @Override
    public void shiftRightClick(Player p) {

        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                120
        );

        if (r == null || r.getHitPosition() == null) return;

        Location center = r.getHitPosition().toLocation(p.getWorld());
        World w = p.getWorld();

        int taskId = Bukkit.getScheduler().runTaskTimer(
                Bukkit.getPluginManager().getPlugin("SpecialSmp"),
                new Runnable() {
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (ticks >= 100) { // 5 seconds
                            Bukkit.getScheduler().cancelTask(taskId);
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

                        // instant blast on ground
                        Bukkit.getScheduler().runTaskTimer(
                                Bukkit.getPluginManager().getPlugin("SpecialSmp"),
                                task -> {
                                    if (tnt.isDead() || !tnt.isValid()) {
                                        task.cancel();
                                        return;
                                    }
                                    if (tnt.isOnGround()) {
                                        Location l = tnt.getLocation();
                                        tnt.remove();
                                        w.createExplosion(l, 6f, true, true, p);
                                        task.cancel();
                                    }
                                },
                                0L, 1L
                        );

                        ticks += 10;
                    }
                },
                0L, 10L
        ).getTaskId();
    }
}
