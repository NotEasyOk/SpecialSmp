package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class DeathListener implements Listener {

    private final Map<UUID, List<ItemStack>> savedCards = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity();
        
        // PvP death mein normal behavior (drops allow)
        if (dead.getKiller() != null) {
            return;
        }

        List<ItemStack> toSave = new ArrayList<>();
        Iterator<ItemStack> it = e.getDrops().iterator();
        
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");

        while (it.hasNext()) {
            ItemStack item = it.next();

            if (item == null || !item.hasItemMeta()) continue;

            // 1. Check for Special Cards (Tag Based)
            boolean isCard = item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING);
            
            // 2. Check for Task Book (Name Based)
            boolean isTaskBook = item.getType() == Material.WRITTEN_BOOK && 
                                 item.getItemMeta().getDisplayName().contains("Aaj Ka Task");

            if (isCard || isTaskBook) {
                toSave.add(item);
                it.remove(); // Zameen par nahi girega
            }
        }

        if (!toSave.isEmpty()) {
            savedCards.put(dead.getUniqueId(), toSave);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        if (savedCards.containsKey(p.getUniqueId())) {
            List<ItemStack> items = savedCards.get(p.getUniqueId());

            for (ItemStack item : items) {
                p.getInventory().addItem(item);
            }

            p.sendMessage("§a§l✔ §fAapke Cards aur Task Book save kar liye gaye!");
            savedCards.remove(p.getUniqueId());
        }
    }
                }
