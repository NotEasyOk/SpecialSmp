package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.cards.UltimateCard;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
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

    private static final Set<PotionEffectType> GOOD_EFFECTS = new HashSet<>();

    static {
        GOOD_EFFECTS.add(PotionEffectType.SPEED);
        GOOD_EFFECTS.add(PotionEffectType.HASTE);
        GOOD_EFFECTS.add(PotionEffectType.STRENGTH);
        GOOD_EFFECTS.add(PotionEffectType.RESISTANCE);
        GOOD_EFFECTS.add(PotionEffectType.FIRE_RESISTANCE);
        GOOD_EFFECTS.add(PotionEffectType.REGENERATION);
        GOOD_EFFECTS.add(PotionEffectType.NIGHT_VISION);
        GOOD_EFFECTS.add(PotionEffectType.SATURATION);
    }

    public UltimateHoldListener() {
        // ✅ Periodic Task: Effects aur Orbit Trigger dono check karega
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isHoldingUltimateCard(p)) {
                        // 1. Give Effects
                        for (PotionEffectType type : GOOD_EFFECTS) {
                            p.addPotionEffect(new PotionEffect(type, 60, 1, false, false, true));
                        }
                        
                        // 2. Start Orbit (UltimateCard class ke andar pehle se check hai ki repeat na ho)
                        BaseCard card = CardRegistry.getCards().get("Ultimate Card");
                        if (card instanceof UltimateCard uc) {
                            uc.startOrbit(p);
                        }
                    }
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    // ✅ Orbit ko fast trigger karne ke liye jaise hi slot change ho
    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItem(e.getNewSlot());
        
        if (isUltimateItem(item)) {
            BaseCard card = CardRegistry.getCards().get("Ultimate Card");
            if (card instanceof UltimateCard uc) {
                uc.startOrbit(p);
            }
        }
    }

    private boolean isHoldingUltimateCard(Player p) {
        return isUltimateItem(p.getInventory().getItemInMainHand());
    }

    // ✅ Secure NBT Check
    private boolean isUltimateItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        String id = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        
        return "Ultimate Card".equals(id);
    }
                                }
