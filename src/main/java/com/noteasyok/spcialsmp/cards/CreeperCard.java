package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreeperCard extends BaseCard {

    private final Map<String, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Creeper Card";
    }
    
      @Override
       public int getModelData() {
          return 0;
    }

      @Override
        public Material getMaterial() {
           return Material.DISC_FRAGMENT_5;
     }
    
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

    /* ================= RIGHT CLICK (Orbital Strike with Animation) ================= */
    @Override
    public void rightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.creeper.right_click_cooldown", 30);
        if (!isCool(p, "orbital", cd)) return;

        RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 120);
        if (r == null || r.getHitPosition() == null) {
            // Remove cooldown if target not found
            cooldowns.remove(p.getUniqueId().toString() + "_orbital");
            return;
        }

        World w = p.getWorld();
        Location hit = r.getHitPosition().toLocation(w);
        Location spawn = hit.clone().add(0, 35, 0);

        TNTPrimed tnt = w.spawn(spawn, TNTPrimed.class);
        tnt.setVelocity(new Vector(0, -2.5, 0));
        tnt.setFuseTicks(200);
        tnt.setYield(10f);
        tnt.setIsIncendiary(false);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!tnt.isValid() || tnt.isDead()) {
                    this.cancel();
                    return;
                }

                // Yellow trail effect
                w.spawnParticle(Particle.FLAME, tnt.getLocation(), 5, 0.1, 0.1, 0.1, 0.05);
                
                if (tnt.isOnGround() || tnt.getLocation().getY() <= hit.getY() + 0.5) {
                    Location l = tnt.getLocation();
                    tnt.remove();
                    w.createExplosion(l, 10f, true, true, p);
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ================= SHIFT + RIGHT CLICK (TNT Rain) ================= */
    @Override
    public void shiftRightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.creeper.shift_click_cooldown", 60);
        if (!isCool(p, "rain", cd)) return;

        RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 120);
        if (r == null || r.getHitPosition() == null) {
            cooldowns.remove(p.getUniqueId().toString() + "_rain");
            return;
        }

        World w = p.getWorld();
        Location center = r.getHitPosition().toLocation(w);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) { cancel(); return; }

                Location spawn = center.clone().add((Math.random() * 8) - 4, 30, (Math.random() * 8) - 4);
                TNTPrimed tnt = w.spawn(spawn, TNTPrimed.class);
                tnt.setVelocity(new Vector(0, -2.5, 0));
                tnt.setFuseTicks(200);
                tnt.setYield(6f);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!tnt.isValid()) { this.cancel(); return; }
                        
                        w.spawnParticle(Particle.FLAME, tnt.getLocation(), 3, 0.1, 0.1, 0.1, 0.02);

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

    // --- COOLDOWN HELPER ---
    private boolean isCool(Player p, String key, int seconds) {
        if (seconds <= 0) return true;
        long now = System.currentTimeMillis();
        String mapKey = p.getUniqueId().toString() + "_" + key;
        
        if (cooldowns.containsKey(mapKey)) {
            long timeLeft = (cooldowns.get(mapKey) - now) / 1000;
            if (timeLeft > 0) {
                String rawMsg = SpcialSmp.get().getConfig().getString("messages.cooldown-active", "§cWait %time%s");
                p.sendMessage(rawMsg.replace("%time%", String.valueOf(timeLeft)));
                return false;
            }
        }
        cooldowns.put(mapKey, now + (seconds * 1000L));
        return true;
    }
            }
