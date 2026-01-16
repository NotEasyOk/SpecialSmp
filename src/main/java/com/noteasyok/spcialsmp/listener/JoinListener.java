package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    public JoinListener() {
        // No cards needed here
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        var data = SpcialSmp.get().getPlayerDataManager();

        // first time join check
        if (!data.hasJoinedBefore(p.getUniqueId())) {
            data.setJoinedBefore(p.getUniqueId(), true);

            p.sendMessage("§aWelcome to Special SMP!");
            p.sendMessage("§eCards can be obtained by crafting.");
            p.sendMessage("§cUltimate Card can ONLY be crafted using all 9 cards.");
        }
    }
}
