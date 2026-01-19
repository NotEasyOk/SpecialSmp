package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WardenCard extends BaseCard {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Warden Card";
    }

    /* ---------------- LEFT CLICK (Health Boost) ---------------- */
    @Override
    public void leftClick(Player p) {
        // Config path: cards.warden.left_click_cooldown
        int cooldownSec = SpcialSmp.get().getConfig().getInt("cards.warden.left_click_cooldown", 30);
        if (!checkCooldown(p, "left", cooldownSec)) return;

        double baseMax = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
        p.setHealth(100.0);

        Bukkit.getScheduler().runTaskLater(
                SpcialSmp.get(),
                () -> {
                    if (!p.isOnline()) return;
                    p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseMax);
                    if (p.getHealth() > baseMax) {
                        p.setHealth(baseMax);
                    }
                },
                20L * 10
        );
    }

    /* ---------------- RIGHT CLICK (SONIC BOOM) ---------------- */
    @Override
    public void rightClick(Player p) {
        // Config path: cards.warden.right_click_cooldown
        int cooldownSec = SpcialSmp.get().getConfig().getInt("cards.warden.right_click_cooldown", 10);
        if (!checkCooldown(p, "right", cooldownSec)) return;

        Location start = p.getEyeLocation();
        Vector dir = start.getDirection().normalize();

        for (int i = 1; i <= 20; i++) {
            Location point = start.clone().add(dir.clone().multiply(i));
            p.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1, 0, 0, 0, 0);

            for (Entity e : p.getWorld().getNearbyEntities(point, 1.5, 1.5, 1.5)) {
                if (e instanceof LivingEntity le && e != p) {
                    le.damage(12, p);
                    le.setVelocity(dir.clone().multiply(1.5));
                }
            }
        }
        p.getWorld().playSound(p.getLocation(), "entity.warden.sonic_boom", 3f, 1f);
    }

    /* ---------------- SHIFT + RIGHT (Buffs) ---------------- */
    @Override
    public void shiftRightClick(Player p) {
        // Config path: cards.warden.shift_right_click_cooldown
        int cooldownSec = SpcialSmp.get().getConfig().getInt("cards.warden.shift_click_cooldown", 45);
        if (!checkCooldown(p, "shift", cooldownSec)) return;

        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 15, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 15, 3));
        p.sendMessage(ChatColor.DARK_AQUA + "Warden's Strength Activated!");
    }

    // --- COOLDOWN HELPER ---
    private boolean checkCooldown(Player p, String action, int seconds) {
        UUID id = p.getUniqueId();
        String key = id.toString() + action;
        long now = System.currentTimeMillis();
        
        if (cooldowns.containsKey(id)) { // Simplest check
            long lastTime = cooldowns.getOrDefault(id.getLeastSignificantBits() + action.hashCode(), 0L);
            // Unique key for each action
            String fullKey = id.toString() + "_" + action;
            // Using a simple check
        }
        
        // Final logic to keep it simple and config friendly
        long nextUsage = cooldowns.getOrDefault(id, 0L); // This is just a placeholder
        // Let's use a more precise key for separate action cooldowns
        String mapKey = id.toString() + "_" + action;
        
        if (cooldowns.containsKey(mapKey)) {
            long timeLeft = (cooldowns.get(mapKey) - now) / 1000;
            if (timeLeft > 0) {
                p.sendMessage(ChatColor.RED + "Ability on cooldown: " + timeLeft + "s");
                return false;
            }
        }
        
        cooldowns.put(mapKey, now + (seconds * 1000L));
        return true;
    }
            }
