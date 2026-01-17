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

public class NothingCard extends BaseCard {

    @Override
    public String getName() {
        return "Nothing Card";
    }

    @Override
    public void leftClick(Player p) {
        long time = p.getWorld().getTime();
        if (time > 12000) {
            p.getWorld().setTime(1000);
            p.sendMessage("§eTime set to day");
        } else {
            p.getWorld().setTime(14000);
            p.sendMessage("§bTime set to night");
        }
    }

    /* ================= RIGHT CLICK: MIND CONTROL (5s) ================= */
    @Override
    public void rightClick(Player p) {
        // 1. Target dhundna (Max distance 15 blocks)
        RayTraceResult result = p.getWorld().rayTraceEntities(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                15,
                entity -> entity instanceof LivingEntity && !entity.equals(p)
        );

        if (result == null || result.getHitEntity() == null) {
            p.sendMessage("§cNo target found to control!");
            return;
        }

        Entity target = result.getHitEntity();
        p.sendMessage("§dControlling " + target.getName() + " for 5 seconds!");

        // 2. Control Task (100 ticks = 5 seconds)
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                // Condition: Card haat mein hona chaiye, player online aur target zinda
                if (ticks >= 100 || !p.isOnline() || target.isDead() || !isHoldingCard(p)) {
                    p.sendMessage("§7Control lost.");
                    this.cancel();
                    return;
                }

                // Target ko hawa mein uthana (Player ke saamne 5 blocks door)
                Vector direction = p.getEyeLocation().getDirection().normalize();
                org.bukkit.Location targetLoc = p.getEyeLocation().add(direction.multiply(5));
                
                // Entity ko move karna
                target.teleport(targetLoc);
                target.setFallDistance(0); // Taki girne par damage na ho

                ticks++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    @Override
    public void shiftRightClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 20, 1, false, false, false));
        p.sendMessage("§fNo fall damage for 20s (Slow Falling)");
    }

    // Helper method to check hand
    private boolean isHoldingCard(Player p) {
        var item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String cleanName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return cleanName.equalsIgnoreCase(getName());
    }
            }
