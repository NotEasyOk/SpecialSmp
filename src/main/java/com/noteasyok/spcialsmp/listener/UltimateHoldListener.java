package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class UltimateHoldListener implements Listener {

    private static final Set<PotionEffectType> GOOD_EFFECTS = new HashSet<>();

    static {
        GOOD_EFFECTS.add(PotionEffectType.SPEED);
        GOOD_EFFECTS.add(PotionEffectType.HASTE);
        GOOD_EFFECTS.add(PotionEffectType.STRENGTH);
        GOOD_EFFECTS.add(PotionEffectType.JUMP_BOOST);
        GOOD_EFFECTS.add(PotionEffectType.REGENERATION);
        GOOD_EFFECTS.add(PotionEffectType.RESISTANCE);
        GOOD_EFFECTS.add(PotionEffectType.FIRE_RESISTANCE);
        GOOD_EFFECTS.add(PotionEffectType.WATER_BREATHING);
        GOOD_EFFECTS.add(PotionEffectType.NIGHT_VISION);
        GOOD_EFFECTS.add(PotionEffectType.ABSORPTION);
        GOOD_EFFECTS.add(PotionEffectType.HEALTH_BOOST);
        GOOD_EFFECTS.add(PotionEffectType.SATURATION);
        GOOD_EFFECTS.add(PotionEffectType.LUCK);
    }

    public UltimateHoldListener() {
        // ✅ Ek repeat hone wala task jo har 20 ticks (1 second) mein check karega
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isHoldingUltimateCard(p)) {
                        // Agar haat mein hai to effects do (40 ticks tak taaki gap na aaye)
                        for (PotionEffectType type : GOOD_EFFECTS) {
                            p.addPotionEffect(new PotionEffect(type, 40, 1, false, false, true));
                        }
                    }
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    // Helper method check karne ke liye ki Ultimate Card pakda hai ya nahi
    private boolean isHoldingUltimateCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        
        // Color strip karke check karna safe hota hai
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name.equals("Ultimate Card");
    }
    }
