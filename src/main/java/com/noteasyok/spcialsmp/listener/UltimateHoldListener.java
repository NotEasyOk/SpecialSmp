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

import java.util.HashSet;
import java.util.Set;

public class UltimateHoldListener implements Listener {

    private static final Set<PotionEffectType> ULTIMATE_EFFECTS = new HashSet<>();

    static {
        ULTIMATE_EFFECTS.add(PotionEffectType.SPEED);
        ULTIMATE_EFFECTS.add(PotionEffectType.HASTE);
        ULTIMATE_EFFECTS.add(PotionEffectType.STRENGTH);
        ULTIMATE_EFFECTS.add(PotionEffectType.JUMP_BOOST);
        ULTIMATE_EFFECTS.add(PotionEffectType.REGENERATION);
        ULTIMATE_EFFECTS.add(PotionEffectType.RESISTANCE);
        ULTIMATE_EFFECTS.add(PotionEffectType.FIRE_RESISTANCE);
        ULTIMATE_EFFECTS.add(PotionEffectType.WATER_BREATHING);
        ULTIMATE_EFFECTS.add(PotionEffectType.NIGHT_VISION);
        ULTIMATE_EFFECTS.add(PotionEffectType.ABSORPTION);
        ULTIMATE_EFFECTS.add(PotionEffectType.HEALTH_BOOST);
        ULTIMATE_EFFECTS.add(PotionEffectType.SATURATION);
        ULTIMATE_EFFECTS.add(PotionEffectType.LUCK);
        ULTIMATE_EFFECTS.add(PotionEffectType.CONDUIT_POWER);
        ULTIMATE_EFFECTS.add(PotionEffectType.DOLPHINS_GRACE);
        // ✅ Poison effect add kiya gaya hai
        ULTIMATE_EFFECTS.add(PotionEffectType.POISON); 
    }

    public UltimateHoldListener() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isUltimate(p.getInventory().getItemInMainHand())) {
                        // Sare effects apply karo
                        for (PotionEffectType type : ULTIMATE_EFFECTS) {
                            p.addPotionEffect(new PotionEffect(type, 60, 1, false, false, true));
                        }
                        
                        // Orbit start karo agar nahi chal raha
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
        if (isUltimate(e.getPlayer().getInventory().getItem(e.getNewSlot()))) {
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
