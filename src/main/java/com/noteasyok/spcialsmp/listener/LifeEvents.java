package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.AnimationManager;
import com.noteasyok.spcialsmp.manager.HeartManager;
import com.noteasyok.spcialsmp.manager.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class LifeEvents implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!SpcialSmp.get().getConfig().getBoolean("life-system.enabled")) return;
        
        // Join karte hi HUD update karo
        HeartManager.updateActionBar(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (!SpcialSmp.get().getConfig().getBoolean("life-system.enabled")) return;

        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        // ANIMATION: Har maut par animation play hogi (Cool lagne ke liye)
        AnimationManager.playSoulLeavingEffect(victim.getLocation(), victim);

        // LOGIC: Sirf tab life kato jab Player ne mara ho
        if (killer != null && killer != victim) {
            PlayerDataManager data = SpcialSmp.get().getPlayerDataManager();
            int currentLives = data.getLives(victim.getUniqueId());

            if (currentLives > 0) {
                // 1. Life Remove
                int newLives = currentLives - 1;
                data.setLives(victim.getUniqueId(), newLives);

                // 2. Messages
                victim.sendMessage("§c§lSOUL RIPPED! §7You lost a life to " + killer.getName());
                killer.sendMessage("§a§lSOUL CONSUMED! §7You stole a life.");
                
                // 3. Ban Logic
                if (newLives <= 0) {
                   Bukkit.getScheduler().runTask(SpcialSmp.get(), () -> {
                       victim.kickPlayer("§c§lGAME OVER\n\n§7Your soul has completely faded away.");
                       Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(victim.getName(), "§c0 Lives Remaining", null, "Console");
                   });
                }
            }
        } else {
            // Natural Death (Message only)
            victim.sendMessage("§eYou died naturally. No life lost.");
        }
    }
}
