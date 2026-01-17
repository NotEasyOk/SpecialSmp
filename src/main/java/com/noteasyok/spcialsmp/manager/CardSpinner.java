package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class CardSpinner {

    public static void openSpinGUI(Player player) {
        // 3-row inventory
        Inventory inv = Bukkit.createInventory(null, 27, "§8» §0§lCARD SELECTION");
        player.openInventory(inv);

        // Ultimate card ko chhod kar pool banayein
        List<BaseCard> allCards = CardRegistry.getCards().values().stream()
                .filter(c -> !c.getName().equalsIgnoreCase("Ultimate Card"))
                .collect(Collectors.toList());

        // Decoration
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack pointer = new ItemStack(Material.RED_STAINED_GLASS_PANE);

        for (int i = 0; i < 27; i++) {
            if (i == 4 || i == 22) inv.setItem(i, pointer);
            else if (i < 9 || i > 17) inv.setItem(i, border);
        }

        new BukkitRunnable() {
            int ticks = 0;
            double speed = 1.0;
            final Random random = new Random();

            @Override
            public void run() {
                if (ticks % (int)speed == 0) {
                    // Items ko left shift karna
                    for (int i = 9; i < 17; i++) {
                        inv.setItem(i, inv.getItem(i + 1));
                    }
                    
                    // Naya random card end mein daalna
                    BaseCard nextCard = allCards.get(random.nextInt(allCards.size()));
                    inv.setItem(17, nextCard.getItemStackWithLore(nextCard.getName()));
                    
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.6f, 1.2f);
                }

                ticks++;
                if (ticks > 40) speed = 2.0;
                if (ticks > 70) speed = 4.0;

                if (ticks >= 100) {
                    this.cancel();
                    ItemStack winner = inv.getItem(13); // Beech wala winner hai
                    if (winner != null) {
                        player.getInventory().addItem(winner);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                        
                        String cardName = winner.getItemMeta().getDisplayName();
                        SpcialSmp.get().getPlayerDataManager().setReceivedFirstCard(player.getUniqueId(), cardName);
                        
                        player.sendMessage("§a§l✔ §fYou won: " + cardName);
                        
                        // 2 second baad close
                        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), player::closeInventory, 40L);
                    }
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }
                      }
