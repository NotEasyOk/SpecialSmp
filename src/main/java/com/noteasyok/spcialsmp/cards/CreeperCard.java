package com.noteasyok.spcialsmp.cards;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.RayTraceResult;

public class CreeperCard implements Card {

    @Override
    public String getName() {
        return "Creeper Card";
    }

    @Override
    public void leftClick(Player p) {
        Location loc = p.getTargetBlockExact(12) != null
                ? p.getTargetBlockExact(12).getLocation().add(0,1,0)
                : p.getLocation();

        p.getWorld().createExplosion(loc, 5.0f, true, true);
    }

    @Override
    public void rightClick(Player p) {
        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                120
        );

        Location hit = (r != null && r.getHitPosition() != null)
                ? r.getHitPosition().toLocation(p.getWorld())
                : p.getLocation();

        Location spawn = hit.clone().add(0, 60, 0);
        TNTPrimed tnt = p.getWorld().spawn(spawn, TNTPrimed.class);
        tnt.setFuseTicks(40);
        tnt.setYield(20f); // 5x TNT
    }

    @Override public void shiftRightClick(Player p) {}
}
