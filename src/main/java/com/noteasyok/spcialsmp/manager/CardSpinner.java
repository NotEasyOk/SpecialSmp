package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CardSpinner {

    public static void openSpinGUI(Player player) {

        // Filter out the Ultimate Card
        List<BaseCard> allCards = CardRegistry.getEnabledCards().stream()
          .filter(c -> !c.getName().equalsIgnoreCase("Ultimate Card"))
          .collect(Collectors.toList());
        
        if (allCards.isEmpty()) return;

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 30; 
            final Random random = new Random();

            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                if (ticks >= maxTicks) {
                    this.cancel();
                    BaseCard winner = allCards.get(random.nextInt(allCards.size()));
                    finishSpin(player, winner);
                    return;
                }

                BaseCard current = allCards.get(random.nextInt(allCards.size()));
                player.sendTitle("§e§l" + current.getName(), "§f§lRolling...", 0, 5, 0);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.6f, 2.0f);
                
                ticks++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 2L); 
    }

    private static void finishSpin(Player player, BaseCard winner) {

        // 2. Mark as RECEIVED immediately (Database update)
        SpcialSmp.get().getPlayerDataManager().setReceivedFirstCard(player.getUniqueId(), winner.getName());

        // 3. Get the PERFECT Lore Item (CardRegistry wala)
        ItemStack finalCard = CardRegistry.getCardItem(winner); 
        
        // 4. Give ONLY ONE item
        player.getInventory().addItem(finalCard);
        
        // 5. Winning Feedback
        player.sendTitle("§a§l★ " + winner.getName() + " ★", "§fNew Card Unlocked!", 10, 60, 20);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);

        // Chat Confirmation
        player.sendMessage("§8§m-----------------------------------------");
        player.sendMessage("§a§l✔ §fYou have successfully unlocked the §e§l" + winner.getName());
        player.sendMessage("§8§m-----------------------------------------");
    }
                           }
