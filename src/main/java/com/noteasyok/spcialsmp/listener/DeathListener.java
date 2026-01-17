package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
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

    // Yahan hum marne wale players ke cards temporary save karenge
    private final Map<UUID, List<ItemStack>> savedCards = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity();
        
        // 1. Agar kisi player ne maara hai (PvP), toh cards DROP hone do (Normal Minecraft behavior)
        if (dead.getKiller() != null) {
            return;
        }

        // 2. Natural Death (Lava, Fall, Mob, etc.) -> Cards Bachao
        List<ItemStack> toSave = new ArrayList<>();
        Iterator<ItemStack> it = e.getDrops().iterator();
        
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");

        while (it.hasNext()) {
            ItemStack item = it.next();

            // Null check
            if (item == null || !item.hasItemMeta()) continue;

            // CHECK: Kya is item par hamara special TAG laga hai?
            // (Naam se check karne se behtar hai Tag check karna)
            if (item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                
                // Item ko list mein save karo
                toSave.add(item);
                
                // Zameen par girne se roko (Remove from drops)
                it.remove();
            }
        }

        // Agar koi card bachaya gaya hai, toh map mein store kar lo
        if (!toSave.isEmpty()) {
            savedCards.put(dead.getUniqueId(), toSave);
        }
    }

    // 3. Jab player wapas zinda hoga (Respawn), use cards wapas de do
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        if (savedCards.containsKey(p.getUniqueId())) {
            List<ItemStack> items = savedCards.get(p.getUniqueId());

            for (ItemStack item : items) {
                p.getInventory().addItem(item);
            }

            p.sendMessage("§a§l✔ §fYour cards were saved from natural death!");
            
            // Dene ke baad list saaf kar do memory bachane ke liye
            savedCards.remove(p.getUniqueId());
        }
    }
                          }
