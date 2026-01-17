package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardSpinner;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        var dataManager = SpcialSmp.get().getPlayerDataManager();

        // 1. FIX: Player agar giant ya tiny mode mein leave kiya tha, toh use reset karo
        // Ye 1.21.1 versions ke liye bahut zaroori hai
        AttributeInstance scale = p.getAttribute(Attribute.GENERIC_SCALE);
        if (scale != null) {
            scale.setBaseValue(1.0);
        }
        p.setGlowing(false); // Herobrine card effect cleanup

        // 2. CHECK: Kya player ko pehla card mil chuka hai?
        if (dataManager.hasReceivedFirstCard(p.getUniqueId())) {
            return;
        }

        // 3. ACTION: Direct item dene ki jagah Animation (Spin) dikhao
        // Pehle hi data update kar do taki koi leave karke baar-baar spin na le sake
        dataManager.setReceivedFirstCard(p.getUniqueId(), "SPINNING...");

        // 4. SPIN GUI OPEN: Ye humare CardSpinner class ko call karega
        // Isme automatically Ultimate card nahi aayega (wo logic Spinner mein hai)
        CardSpinner.openSpinGUI(p);
        
        p.sendMessage("§e§l⚡ §fSelecting your special card... Prepare yourself!");
    }
}
