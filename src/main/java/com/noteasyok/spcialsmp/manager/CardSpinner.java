package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CardSpinner {

    public static void openSpinGUI(Player player) {
        // Filter out the Ultimate Card from the pool
        List<BaseCard> allCards = CardRegistry.getCards().values().stream()
                .filter(c -> !c.getName().equalsIgnoreCase("Ultimate Card"))
                .collect(Collectors.toList());

        if (allCards.isEmpty()) return;

        // Set location in front of the player
        Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(2)).add(0, 0.8, 0);
        
        // Spawn invisible Armor Stand for the animation
        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setSmall(true);
        stand.setCustomNameVisible(true);
        stand.setInvulnerable(true);

        new BukkitRunnable() {
            int ticks = 0;
            final Random random = new Random();

            @Override
            public void run() {
                // End spin after 40 ticks (~2 seconds) or if player leaves
                if (!player.isOnline() || ticks >= 40) {
                    this.cancel();
                    
                    // Select final winner
                    BaseCard winner = allCards.get(random.nextInt(allCards.size()));
                    finishSpin(player, stand, winner);
                    return;
                }

                // Rapidly swap card and name (One-by-one style)
                BaseCard current = allCards.get(random.nextInt(allCards.size()));
                
                // Update Item and Name
                stand.getEquipment().setHelmet(current.getItemStackWithLore(current.getName()));
                stand.setCustomName("§f§l" + current.getName());

                // Smooth rotation animation
                Location teleportLoc = stand.getLocation();
                teleportLoc.setYaw(teleportLoc.getYaw() + 30f);
                stand.teleport(teleportLoc);

                // Tick sound effect
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.8f);
                
                ticks++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 2L); // Run every 2 ticks for a smoother cycling look
    }

    private static void finishSpin(Player player, ArmorStand stand, BaseCard winner) {
        // Display winning card
        ItemStack winningItem = winner.getItemStackWithLore(winner.getName());
        stand.getEquipment().setHelmet(winningItem);
        stand.setCustomName("§a§l★ " + winner.getName() + " ★");

        // Professional English Title (Hinglish removed)
        player.sendTitle("§a§l" + winner.getName(), "§fYou received this card!", 10, 40, 10);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        
        // Give card to player and save data
        player.getInventory().addItem(winningItem);
        SpcialSmp.get().getPlayerDataManager().setReceivedFirstCard(player.getUniqueId(), winner.getName());

        // English confirmation message
        player.sendMessage("§a§l✔ §fSuccess! You have been awarded the §e" + winner.getName());

        // Remove armor stand after 3 seconds
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), stand::remove, 60L);
    }
                    }
