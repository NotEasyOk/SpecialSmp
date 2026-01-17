package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.cards.UltimateCard;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class UltimateHoldListener implements Listener {

    public UltimateHoldListener() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isUltimate(p.getInventory().getItemInMainHand())) {
                        // All effects
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1, false, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 1, false, false));
                        
                        // Start Orbit
                        BaseCard card = CardRegistry.getCards().get("Ultimate Card");
                        if (card instanceof UltimateCard uc) {
                            uc.startOrbit(p);
                        }
                    }
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    @EventHandler
    public void onSwap(PlayerItemHeldEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
        if (isUltimate(item)) {
            BaseCard card = CardRegistry.getCards().get("Ultimate Card");
            if (card instanceof UltimateCard uc) uc.startOrbit(e.getPlayer());
        }
    }

    private boolean isUltimate(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        String id = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (id != null && id.equals("Ultimate Card")) return true;
        
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name.equals("Ultimate Card");
    }
    }
