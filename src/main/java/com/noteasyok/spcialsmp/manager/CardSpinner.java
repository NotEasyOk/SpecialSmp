package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CardSpinner {

    public static void openSpinGUI(Player player) {
        // Filter out the Ultimate Card
        List<BaseCard> allCards = CardRegistry.getCards().values().stream()
                .filter(c -> !c.getName().equalsIgnoreCase("Ultimate Card"))
                .collect(Collectors.toList());

        if (allCards.isEmpty()) return;

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 30; // Total animation steps
            final Random random = new Random();

            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                // Animation End Logic
                if (ticks >= maxTicks) {
                    this.cancel();
                    
                    // Select Final Winner
                    BaseCard winner = allCards.get(random.nextInt(allCards.size()));
                    finishSpin(player, winner);
                    return;
                }

                // Rapidly cycling through cards (Video style)
                BaseCard current = allCards.get(random.nextInt(allCards.size()));
                
                // Show on Screen (Title as Name, Subtitle as the cycling effect)
                // 0 stay time, 5 fade in/out for rapid "flashing" look
                player.sendTitle("§e§l" + current.getName(), "§f§lRolling...", 0, 5, 0);

                // "Tick" Sound (High pitch for rolling feel)
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.6f, 2.0f);
                
                ticks++;
            }
            // Run every 2 ticks (0.1 seconds) for that fast video cycling speed
        }.runTaskTimer(SpcialSmp.get(), 0L, 2L); 
    }

    private static void finishSpin(Player player, BaseCard winner) {
        // Final Winning Display (Big Green Title)
        player.sendTitle("§a§l★ " + winner.getName() + " ★", "§fNew Card Unlocked!", 10, 60, 20);
        
        // Winning Sound
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);
        
        // Give Item and Save Data
        player.getInventory().addItem(winner.getItemStackWithLore(winner.getName()));
        SpcialSmp.get().getPlayerDataManager().setReceivedFirstCard(player.getUniqueId(), winner.getName());

        // Chat Confirmation
        player.sendMessage("§8§m-----------------------------------------");
        player.sendMessage("§a§l✔ §fYou have successfully unlocked the §e§l" + winner.getName());
        player.sendMessage("§8§m-----------------------------------------");
    }
}
