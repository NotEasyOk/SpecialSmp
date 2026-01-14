package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class ZombieOwnerListener implements Listener {

    @EventHandler
    public void onTarget(EntityTargetEvent e) {

        Entity entity = e.getEntity();
        if (!(entity instanceof Zombie zombie)) return;

        if (!zombie.hasMetadata("owner")) return;
        if (!(e.getTarget() instanceof Player player)) return;

        String owner = zombie.getMetadata("owner").get(0).asString();
        if (player.getUniqueId().toString().equals(owner)) {
            e.setCancelled(true);
        }
    }
            }
