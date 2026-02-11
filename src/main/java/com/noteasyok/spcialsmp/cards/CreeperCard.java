package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.TNTPrimed;
import java.util.HashMap;
import java.util.Map;

public class CreeperCard extends BaseCard {

    private final Map<String, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() { return "Creeper Card"; }
    
    @Override
    public int getModelData() { return 0; }

    @Override
    public Material getMaterial() { return Material.DISC_FRAGMENT_5; }
    
    /* ================= LEFT CLICK (Big Explosion) ================= */
    @Override
    public void leftClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.creeper.left_click_cooldown", 10);
        if (!isCool(p, "boom", cd)) return;

        Location loc = p.getTargetBlockExact(12) != null
                ? p.getTargetBlockExact(12).getLocation().add(0, 1, 0)
                : p.getLocation();

        p.getWorld().createExplosion(loc, 5f, true, true, p);
    }

    /* ================= RIGHT CLICK (Orbital Strike - Sync & Particles Fixed) ================= */
    @Override
    public void rightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.creeper.right_click_cooldown", 30);
        if (!isCool(p, "orbital", cd)) return;

        RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 120);
        if (r == null || r.getHitPosition() == null) {
            cooldowns.remove(p.getUniqueId().toString() + "_orbital");
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
                    w.createExplosion(current, 20.0f, true, true, p);
                    
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
    int cd = SpcialSmp.get().getConfig().getInt("cards.creeper.shift_click_cooldown", 60);
    if (!isCool(p, "rain", cd)) return;

    RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 120);
    if (r == null || r.getHitPosition() == null) return;

    World w = p.getWorld();
    Location center = r.getHitPosition().toLocation(w);

    new BukkitRunnable() {
        int ticks = 0;
        @Override
        public void run() {
            // 200 ticks = 10 seconds (Lagatar barish)
            if (ticks >= 200) { cancel(); return; }

            // Har tick par 2 TNT spawn honge taaki rain dense lage
            for (int i = 0; i < 2; i++) {
                Location spawn = center.clone().add((Math.random() * 20) - 10, 30, (Math.random() * 20) - 10);
                TNTPrimed tnt = w.spawn(spawn, TNTPrimed.class);
                
                tnt.setFuseTicks(200); 
                tnt.setVelocity(new Vector(0, -1.5, 0)); // Velocity 1.5 hi rakhi hai

                // Ground touch detection
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (tnt.isDead() || !tnt.isValid()) { this.cancel(); return; }

                        if (tnt.isOnGround() || tnt.getLocation().getBlock().getType().isSolid()) {
                            // Power 10.0F (Bada Dhamaka)
                            w.createExplosion(tnt.getLocation(), 10.0F, true, true); 
                            tnt.remove();
                            this.cancel();
                        }
                    }
                }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
            }
            ticks++;
        }
    }.runTaskTimer(SpcialSmp.get(), 0L, 1L); // Har 1 tick par chalega (Fastest Rain)
        }

    private boolean isCool(Player p, String key, int seconds) {
        if (seconds <= 0) return true;
        long now = System.currentTimeMillis();
        String mapKey = p.getUniqueId().toString() + "_" + key;
        if (cooldowns.containsKey(mapKey)) {
            long timeLeft = (cooldowns.get(mapKey) - now) / 1000;
            if (timeLeft > 0) {
                p.sendMessage("§cWait " + timeLeft + "s");
                return false;
            }
        }
        cooldowns.put(mapKey, now + (seconds * 1000L));
        return true;
    }
                    }
