package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.TNTPrimed;
import java.util.HashMap;
import java.util.Map;

public class CreeperCard extends BaseCard {


    @Override
    public String getName() { return "Creeper Card"; }
    
    @Override
    public int getModelData() { return 0; }

    @Override
    public Material getMaterial() { return Material.DISC_FRAGMENT_5; }

    private boolean isInsideRegion(Location loc) {
    com.sk89q.worldguard.protection.regions.RegionContainer container = com.sk89q.worldguard.protection.WorldGuard.getInstance().getPlatform().getRegionContainer();
    com.sk89q.worldguard.protection.regions.RegionQuery query = container.createQuery();
    return !query.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(loc)).getRegions().isEmpty();
    }
}
    
    /* ================= LEFT CLICK (Big Explosion) ================= */
    @Override
    public void leftClick(Player p) {
        if (!isCool(p, "left")) return;

        Location loc = p.getTargetBlockExact(12) != null
                ? p.getTargetBlockExact(12).getLocation().add(0, 1, 0)
                : p.getLocation();

           if (isInsideRegion(loc)) {
         p.getWorld().createExplosion(loc, 5f, false, false, p); // Safe Explosion
         } else {

        p.getWorld().createExplosion(loc, 5f, true, true, p);
    }
 }

    /* ================= RIGHT CLICK (Orbital Strike - Sync & Particles Fixed) ================= */
    @Override
    public void rightClick(Player p) {
        if (!isCool(p, "right")) return;

        RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 120);
        if (r == null || r.getHitPosition() == null) {
            return;
        }

        World w = p.getWorld();
        final Location hitLoc = r.getHitPosition().toLocation(w); 
        Location spawnLoc = hitLoc.clone().add(0, 35, 0);

        // Bada Block (TNT Visual)
        org.bukkit.entity.BlockDisplay display = w.spawn(spawnLoc, org.bukkit.entity.BlockDisplay.class);
        display.setBlock(Material.TNT.createBlockData());
        
        display.setTransformation(new org.bukkit.util.Transformation(
            new org.joml.Vector3f(-2.5f, 0, -2.5f), 
            new org.joml.Quaternionf(), 
            new org.joml.Vector3f(5.0f, 5.0f, 5.0f), 
            new org.joml.Quaternionf()
        ));

        new BukkitRunnable() {
            double fallSpeed = 1.3; // Constant falling speed for smoothness
            @Override
            public void run() {
                Location current = display.getLocation();
                
                // --- YELLOW PRACTICAL (Mota Effect) ---
                // Particle.DUST yellow color mein, upar ki taraf (vector 0, 0.5, 0)
                Particle.DustOptions yellowDust = new Particle.DustOptions(Color.YELLOW, 2.0f);
                w.spawnParticle(Particle.DUST, current, 25, 1.2, 0.5, 1.2, 0.1, yellowDust);
                // Extra Mota Yellow Flame for intensity
                w.spawnParticle(Particle.FLAME, current, 10, 0.8, 0.2, 0.8, 0.05);

                // Zameen touch detection
                if (current.getY() <= hitLoc.getY() + 0.8 || current.getBlock().getType().isSolid()) {
                    display.remove();
                    
                    // MASSIVE 20 POWER EXPLOSION
                    if (isInsideRegion(current)) {
                 w.createExplosion(current, 20.0f, false, false, p); // Region safe
              } else {
                    w.createExplosion(current, 20.0f, true, true, p);
                    }
                        
                    this.cancel();
                    return;
                }

                // Smoothly moving the display block down
                display.teleport(current.add(0, -fallSpeed, 0));
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ================= SHIFT + RIGHT CLICK (TNT Rain - 10 Sec) ================= */
@Override
public void shiftRightClick(Player p) {
    if (!isCool(p, "shift_right")) return;

    RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 120);
    if (r == null || r.getHitPosition() == null) return;

    World w = p.getWorld();
    Location center = r.getHitPosition().toLocation(w);

    new BukkitRunnable() {
        int count = 0;
        @Override
        public void run() {
            // Sirf 5 TNT spawn honge
            if (count >= 5) { cancel(); return; }

            // Random location (10 block radius)
            Location spawn = center.clone().add((Math.random() * 10) - 5, 25, (Math.random() * 10) - 5);
            TNTPrimed tnt = w.spawn(spawn, TNTPrimed.class);
            
            // TNT Settings
            tnt.setFuseTicks(100); // 5 seconds fuse
            tnt.setVelocity(new Vector(0, -1.0, 0)); // Normal falling speed

            // Ground check (Optional: TNT ko normal phatne do ya turant phadna hai toh ye rakho)
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (tnt.isDead() || !tnt.isValid()) { this.cancel(); return; }

                    if (tnt.isOnGround()) {
                        // 4.0F normal TNT power hoti hai
                        if (isInsideRegion(tntLoc)) {
                            
                      w.createExplosion(tntLoc, 4.0F, false, false); // Safe TNT
                    } else {
                            
                        w.createExplosion(tnt.getLocation(), 4.0F, true, true); 
                        }
                        
                        tnt.remove();
                        this.cancel();
                    }
                }
            }.runTaskTimer(SpcialSmp.get(), 0L, 1L);

            count++;
        }
    }.runTaskTimer(SpcialSmp.get(), 0L, 20L); // 20 ticks = Har 1 second mein ek TNT girega
}

    private boolean isCool(Player p, String action) {
    // Purana 'seconds' wala logic hata do, manager config se khud seconds uthayega
    if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), action)) {
        long remaining = SpcialSmp.get().getCooldownManager().getRemainingSeconds(p, getName(), action);
        p.sendMessage("§cWait " + remaining + "s");
        return false;
    }

    // Cooldown apply manager ke through karo
    SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), action);
    return true;
    }
}
