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

    // Step 1: Cooldown track karne ke liye Map
    private final Map<String, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Enderman Card";
    }

    /* ---------------- LEFT CLICK (Teleport) ---------------- */
    @Override
    public void leftClick(Player p) {
        // Config path: cards.enderman.teleport_cooldown
        int cd = SpcialSmp.get().getConfig().getInt("cards.enderman.teleport_cooldown", 5);
        if (!isCool(p, "tp", cd)) return;

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
        } else {
            // Safe location nahi mili to cooldown reset
            cooldowns.remove(p.getUniqueId().toString() + "tp");
        }
    }

    /* ---------------- RIGHT CLICK (Pull Target) ---------------- */
    @Override
    public void rightClick(Player p) {
        // Config path: cards.enderman.pull_cooldown
        int cd = SpcialSmp.get().getConfig().getInt("cards.enderman.pull_cooldown", 10);
        if (!isCool(p, "pull", cd)) return;

        RayTraceResult r = p.getWorld().rayTraceEntities(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                40,
                e -> e instanceof Player && !e.equals(p)
        );

        if (r == null || r.getHitEntity() == null) {
            cooldowns.remove(p.getUniqueId().toString() + "pull");
            return;
        }

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

    /* ---------------- SHIFT + RIGHT CLICK (Dragon Breath) ---------------- */
    @Override
    public void shiftRightClick(Player p) {
        // Config path: cards.enderman.breath_cooldown
        int cd = SpcialSmp.get().getConfig().getInt("cards.enderman.breath_cooldown", 20);
        if (!isCool(p, "breath", cd)) return;

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
        cloud.setDuration(200); 
        cloud.setWaitTime(0);             
        cloud.setReapplicationDelay(20);  
        cloud.setRadiusOnUse(0.0f);       
        cloud.setRadiusPerTick(0.0f);     
        cloud.setParticle(org.bukkit.Particle.DRAGON_BREATH);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), false);
        cloud.setSource(p); 

        p.sendMessage(ChatColor.DARK_PURPLE + "Dragon Breath Released!");
    }

    // --- COOLDOWN HELPER (Universal) ---
    private boolean isCool(Player p, String key, int seconds) {
    if (seconds <= 0) return true;
    long now = System.currentTimeMillis();
    
    // Map ki key String honi chahiye
    String mapKey = p.getUniqueId().toString() + "_" + key;
    
    if (cooldowns.containsKey(mapKey)) {
        long timeLeft = (cooldowns.get(mapKey) - now) / 1000;
        if (timeLeft > 0) {
            // Config se message uthayega
            String rawMsg = SpcialSmp.get().getConfig().getString("messages.cooldown-active", "§cWait %time%s");
            p.sendMessage(rawMsg.replace("%time%", String.valueOf(timeLeft)));
            return false;
        }
    }
    cooldowns.put(mapKey, now + (seconds * 1000L));
    return true;
    }
