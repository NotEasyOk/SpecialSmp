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
import java.util.UUID;

public class UltimateHoldListener implements Listener {

    private static final Set<PotionEffectType> ULTIMATE_EFFECTS = new HashSet<>();
    private final Set<UUID> currentlyHolding = new HashSet<>(); 

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
        ULTIMATE_EFFECTS.add(PotionEffectType.SATURATION);
        ULTIMATE_EFFECTS.add(PotionEffectType.LUCK);
        ULTIMATE_EFFECTS.add(PotionEffectType.CONDUIT_POWER);
        ULTIMATE_EFFECTS.add(PotionEffectType.DOLPHINS_GRACE);
    }

    public UltimateHoldListener() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    ItemStack item = p.getInventory().getItemInMainHand();
                    if (isUltimate(item)) {
                        // Effects dena (Dono modes mein)
                        for (PotionEffectType type : ULTIMATE_EFFECTS) {
                            p.addPotionEffect(new PotionEffect(type, 65, 1, false, false, true));
                        }

                        if (!currentlyHolding.contains(p.getUniqueId())) {
                            currentlyHolding.add(p.getUniqueId());
                            BaseCard card = CardRegistry.getCards().get("Ultimate Card");
                            // Orbit sirf Green Dye (Normal Mode) par start hoga
                            if (card instanceof UltimateCard uc && item.getType() == Material.GREEN_DYE) {
                                uc.startOrbit(p);
                            }
                        }
                    } else {
                        // Agar haath mein card nahi hai toh state clear karo
                        if (currentlyHolding.contains(p.getUniqueId())) {
                            currentlyHolding.remove(p.getUniqueId());
                            BaseCard card = CardRegistry.getCards().get("Ultimate Card");
                            if (card instanceof UltimateCard uc) uc.stopOrbit(p);
                        }
                    }
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    @EventHandler
    public void onSwap(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        ItemStack newItem = p.getInventory().getItem(e.getNewSlot());

        if (isUltimate(newItem)) {
            if (!currentlyHolding.contains(p.getUniqueId())) {
                currentlyHolding.add(p.getUniqueId());
                BaseCard card = CardRegistry.getCards().get("Ultimate Card");
                // Swap par bhi sirf Green Dye par orbit start
                if (card instanceof UltimateCard uc && newItem.getType() == Material.GREEN_DYE) {
                    uc.startOrbit(p);
                }
            }
        } else {
            currentlyHolding.remove(p.getUniqueId());
            BaseCard card = CardRegistry.getCards().get("Ultimate Card");
            if (card instanceof UltimateCard uc) uc.stopOrbit(p);
        }
    }

    private boolean isUltimate(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        
        // Dono materials ko allow karna zaroori hai
        Material type = item.getType();
        if (type != Material.GREEN_DYE && type != Material.WARPED_FUNGUS_ON_A_STICK) return false;
        
        if (!item.hasItemMeta()) return false;
        
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        String id = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return "Ultimate Card".equals(id);
    }
                                    }
