package com.noteasyok.spcialsmp.cards;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
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
        // Big explosion at player's aim/location (or at player's feet if prefer)
        Location loc = p.getTargetBlockExact(10) != null ? p.getTargetBlockExact(10).getLocation().add(0,1,0) : p.getLocation();
        // power 5f ~ bigger than normal TNT
        p.getWorld().createExplosion(loc, 5f, true, true);
    }

    @Override
    public void rightClick(Player p) {
        // Orbital strike: spawn a large primed TNT high above aimed location
        RayTraceResult rt = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 120);
        Location target = (rt != null && rt.getHitPosition() != null) ? rt.getHitPosition().toLocation(p.getWorld()) : p.getLocation();
        Location spawn = target.clone().add(0, 60, 0); // 60 blocks above
        TNTPrimed big = (TNTPrimed) p.getWorld().spawnEntity(spawn, EntityType.PRIMED_TNT);
        // set short fuse so it falls and explodes near impact (fuse ticks)
        big.setFuseTicks(40); // 2 seconds
        // when it explodes, create bigger explosion at its location — handled by server
    }

    @Override
    public void shiftRightClick(Player p) {
        // TNT rain for 5 seconds: spawn multiple small primed TNTs above players area
        for (int i = 0; i < 20; i++) {
            Location spawn = p.getLocation().add((Math.random()-0.5)*10, 25 + Math.random()*10, (Math.random()-0.5)*10);
            TNTPrimed t = (TNTPrimed) p.getWorld().spawnEntity(spawn, EntityType.PRIMED_TNT);
            t.setFuseTicks(40 + (int)(Math.random()*20));
        }
    }
}
