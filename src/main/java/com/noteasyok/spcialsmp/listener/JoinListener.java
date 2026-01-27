package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardSpinner;
import com.noteasyok.spcialsmp.manager.FuelManager; // Naya import
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        var dataManager = SpcialSmp.get().getPlayerDataManager();

        // --- FUEL SYSTEM START ---
        // Agar naya player hai to use 24 hours fuel do
        if (!p.hasPlayedBefore()) {
            FuelManager.setFuel(p, 1440);
        }
        // --- FUEL SYSTEM END ---

        if (p.getAttribute(Attribute.GENERIC_SCALE) != null) 
            p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);

        if (!dataManager.hasReceivedFirstCard(p.getUniqueId())) {
            dataManager.setReceivedFirstCard(p.getUniqueId(), "SPINNING...");
            CardSpinner.openSpinGUI(p);
        }
    }
}
