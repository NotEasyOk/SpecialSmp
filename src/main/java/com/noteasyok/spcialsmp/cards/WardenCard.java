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

    private final Map<String, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Warden Card";
    }
    
     @Override
public int getModelData() {
    return 9;
}
    
    /* ---------------- LEFT CLICK (Health Boost) ---------------- */
    @Override
    public void leftClick(Player p) {
        int cooldownSec = SpcialSmp.get().getConfig().getInt("cards.warden.left_click_cooldown", 30);
        // FIXED: checkCooldown ki jagah isCool use kiya
        if (!isCool(p, "left", cooldownSec)) return;

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
        int cooldownSec = SpcialSmp.get().getConfig().getInt("cards.warden.right_click_cooldown", 10);
        // FIXED: checkCooldown ki jagah isCool use kiya
        if (!isCool(p, "right", cooldownSec)) return;

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
        p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_WARDEN_SONIC_BOOM, 3f, 1f);
    }

    /* ---------------- SHIFT + RIGHT (Buffs) ---------------- */
    @Override
    public void shiftRightClick(Player p) {
        int cooldownSec = SpcialSmp.get().getConfig().getInt("cards.warden.shift_click_cooldown", 45);
        // FIXED: checkCooldown ki jagah isCool use kiya
        if (!isCool(p, "shift", cooldownSec)) return;

        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 15, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 15, 3));
        p.sendMessage(ChatColor.DARK_AQUA + "Warden's Strength Activated!");
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
