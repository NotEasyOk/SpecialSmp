package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class ZombieOwnerListener implements Listener {

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent e) {
        if (!(e.getEntity() instanceof Zombie)) return;
        if (!(e.getTarget() instanceof Player)) return;

        Zombie zombie = (Zombie) e.getEntity();
        Player target = (Player) e.getTarget();

        if (!zombie.hasMetadata("owner")) return;

        String ownerId = zombie.getMetadata("owner").get(0).asString();

        if (target.getUniqueId().toString().equals(ownerId)) {
            e.setCancelled(true); // owner ko attack nahi karega
        }
    }
}
