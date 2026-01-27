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
        // Ultimate card ko chhod kar baaki cards ka pool filter karna
        List<BaseCard> allCards = CardRegistry.getCards().values().stream()
                .filter(c -> !c.getName().equalsIgnoreCase("Ultimate Card"))
                .collect(Collectors.toList());

        if (allCards.isEmpty()) return;

        // Player ke samne location set karna
        Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(2)).add(0, 0.8, 0);
        
        // Armor Stand spawn karna (Physical animation ke liye)
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
                if (!player.isOnline() || ticks >= 60) {
                    this.cancel();
                    
                    // Final winner select karna
                    BaseCard winner = allCards.get(random.nextInt(allCards.size()));
                    finishSpin(player, stand, winner);
                    return;
                }

                // Randomly change card and name during spin
                BaseCard current = allCards.get(random.nextInt(allCards.size()));
                
                // Item aur Name set karna
                stand.getEquipment().setHelmet(current.getItemStackWithLore(current.getName()));
                stand.setCustomName("§f§l" + current.getName());

                // Rotation animation
                Location teleportLoc = stand.getLocation();
                teleportLoc.setYaw(teleportLoc.getYaw() + 25f);
                stand.teleport(teleportLoc);

                // Spin sound
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.8f);
                
                ticks++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    private static void finishSpin(Player player, ArmorStand stand, BaseCard winner) {
        // Winner card display
        ItemStack winningItem = winner.getItemStackWithLore(winner.getName());
        stand.getEquipment().setHelmet(winningItem);
        stand.setCustomName("§a§l★ " + winner.getName() + " ★");

        // Player effects
        player.sendTitle("§a§l" + winner.getName(), "§fAapko ye card mila!", 10, 40, 10);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        
        // Inventory mein card dena
        player.getInventory().addItem(winningItem);

        // Data save karna
        SpcialSmp.get().getPlayerDataManager().setReceivedFirstCard(player.getUniqueId(), winner.getName());
        player.sendMessage("§a§l✔ §fYou won: " + winner.getName());

        // 3 second baad armor stand remove karna
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), stand::remove, 60L);
    }
            }
