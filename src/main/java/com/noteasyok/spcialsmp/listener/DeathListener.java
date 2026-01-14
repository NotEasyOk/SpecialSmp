package com.noteasyok.spcialsmp.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {

        // Natural death -> NO DROP
        if (e.getEntity().getKiller() == null) {
            e.getDrops().clear();
            return;
        }

        // Player kill -> allow drop (keep items)
        // Tum yahan custom logic bhi add kar sakte ho
    }
}
