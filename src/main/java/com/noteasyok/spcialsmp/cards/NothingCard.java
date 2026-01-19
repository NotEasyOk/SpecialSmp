package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NothingCard extends BaseCard {

    // Cooldown track karne ke liye Map
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Nothing Card";
    }

    /* ================= LEFT CLICK: TIME CHANGE (With Cooldown) ================= */
    @Override
    public void leftClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.nothing.left_click_cooldown", 10);
        if (!isCool(p, "time", cd)) return;

        long time = p.getWorld().getTime();
        if (time > 12000) {
            p.getWorld().setTime(1000);
            p.sendMessage("§eTime set to day");
        } else {
            p.getWorld().setTime(14000);
            p.sendMessage("§bTime set to night");
        }
    }

    /* ================= RIGHT CLICK: MIND CONTROL (With Cooldown) ================= */
    @Override
    public void rightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.nothing.right_click_cooldown", 30);
        
        // Target check karne se pehle cooldown check karna zaroori hai
        if (!isCool(p, "control", cd)) return;

        RayTraceResult result = p.getWorld().rayTraceEntities(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                15,
                entity -> entity instanceof LivingEntity && !entity.equals(p)
        );

        if (result == null || result.getHitEntity() == null) {
            p.sendMessage("§cNo target found to control!");
            // Target nahi mila to cooldown hata dete hain taaki click waste na ho
            cooldowns.remove(p.getUniqueId().toString() + "control");
            return;
        }

        Entity target = result.getHitEntity();
        p.sendMessage("§dControlling " + target.getName() + " for 5 seconds!");

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100 || !p.isOnline() || target.isDead() || !isHoldingCard(p)) {
                    p.sendMessage("§7Control lost.");
                    this.cancel();
                    return;
                }

                Vector direction = p.getEyeLocation().getDirection().normalize();
                org.bukkit.Location targetLoc = p.getEyeLocation().add(direction.multiply(5));
                target.teleport(targetLoc);
                target.setFallDistance(0);
                ticks++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ================= SHIFT CLICK: SLOW FALLING (With Cooldown) ================= */
    @Override
    public void shiftRightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.nothing.shift_click_cooldown", 40);
        if (!isCool(p, "falling", cd)) return;

        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 20, 1, false, false, false));
        p.sendMessage("§fNo fall damage for 20s (Slow Falling)");
    }

    // --- COOLDOWN HELPER (Universal) ---
    private boolean isCool(Player p, String key, int seconds) {
        long now = System.currentTimeMillis();
        String mapKey = p.getUniqueId().toString() + key;
        if (cooldowns.containsKey(mapKey)) {
            long timeLeft = (cooldowns.get(mapKey) - now) / 1000;
            if (timeLeft > 0) {
                p.sendMessage(ChatColor.RED + "Wait " + timeLeft + "s for " + key + "!");
                return false;
            }
        }
        cooldowns.put(mapKey, now + (seconds * 1000L));
        return true;
    }

    private boolean isHoldingCard(Player p) {
        var item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String cleanName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return cleanName.equalsIgnoreCase(getName());
    }
                             }
