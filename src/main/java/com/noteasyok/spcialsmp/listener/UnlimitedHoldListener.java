package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumSet;
import java.util.Set;

public class UnlimitedHoldListener implements Listener {

    // ✅ Only GOOD potion effects
    private static final Set<PotionEffectType> GOOD_EFFECTS = EnumSet.of(
            PotionEffectType.SPEED,
            PotionEffectType.HASTE,
            PotionEffectType.STRENGTH,
            PotionEffectType.JUMP,
            PotionEffectType.REGENERATION,
            PotionEffectType.RESISTANCE,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.WATER_BREATHING,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.ABSORPTION,
            PotionEffectType.HEALTH_BOOST,
            PotionEffectType.SATURATION,
            PotionEffectType.LUCK
    );

    @EventHandler
    public void onItemSwitch(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();

        ItemStack newItem = p.getInventory().getItem(e.getNewSlot());

        // ❌ Agar Unlimited Card haat me NAHI hai
        if (newItem == null
                || !newItem.hasItemMeta()
                || !newItem.getItemMeta().hasDisplayName()
                || !newItem.getItemMeta().getDisplayName().equals("Unlimited Card")) {

            // 🔥 1 second baad sab effects remove
            SpicialSmp.get().getServer().getScheduler().runTaskLater(
                    SpicialSmp.get(),
                    () -> GOOD_EFFECTS.forEach(p::removePotionEffect),
                    20L
            );
            return;
        }

        // ✅ Unlimited Card haat me hai → effects lagao
        for (PotionEffectType type : GOOD_EFFECTS) {
            p.addPotionEffect(
                    new PotionEffect(type, 40, 1, false, false, true)
            );
        }
    }
      }
              
