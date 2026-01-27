package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class FuelManager {

    private static final NamespacedKey FUEL_KEY = new NamespacedKey(SpcialSmp.get(), "soul_fuel");
    private static final int MAX_FUEL = 1440; // 24 hours in minutes

    /**
     * Fuel system ko start karta hai (Main class se call hoga)
     */
    public static void startFuelTask() {
        // Task 1: Har 1 minute mein fuel -1 karega
        Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                reduceFuel(p, 1);
            }
        }, 1200L, 1200L);

        // Task 2: Action bar ko har 2 second mein refresh karega
        Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateActionBar(p);
            }
        }, 40L, 40L);
    }

    public static void setFuel(Player p, int minutes) {
        p.getPersistentDataContainer().set(FUEL_KEY, PersistentDataType.INTEGER, Math.min(minutes, MAX_FUEL));
    }

    public static int getFuel(Player p) {
        return p.getPersistentDataContainer().getOrDefault(FUEL_KEY, PersistentDataType.INTEGER, MAX_FUEL);
    }

    public static void reduceFuel(Player p, int amount) {
        int current = getFuel(p);
        int next = current - amount;

        if (next <= 0) {
            setFuel(p, 0);
            handleBan(p);
        } else {
            setFuel(p, next);
            // Agar fuel 1 ghante se kam bacha ho toh warning sound
            if (next <= 60 && next % 10 == 0) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                p.sendMessage("§c§lWARNING: §7Aapka Soul Fuel khatam hone wala hai!");
            }
        }
    }

    private static void handleBan(Player p) {
        Bukkit.getScheduler().runTask(SpcialSmp.get(), () -> {
            p.getWorld().strikeLightningEffect(p.getLocation()); // Fancy Lightning sound
            p.sendMessage("§c§lAapka waqt khatam ho gaya...");
            
            // 2 second ka delay taaki player message dekh sake
            Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
                p.kickPlayer("§c§lSOUL DEPLETED\n\n§7Aapka fuel khatam ho gaya hai.\n§eAgli baar task time par pura karein!");
            }, 40L);
        });
    }

    public static void updateActionBar(Player p) {
        int fuel = getFuel(p);
        int hours = fuel / 60;
        int mins = fuel % 60;

        // Progress Bar (10 Blocks)
        int totalBars = 10;
        int filledBars = (int) (((double) fuel / MAX_FUEL) * totalBars);
        
        StringBuilder barStr = new StringBuilder("§8[");
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                barStr.append("§a┃"); // Green for filled
            } else {
                barStr.append("§r┃"); // Gray for empty
            }
        }
        barStr.append("§8]");

        // Color toggle: 5 ghante se kam par Red
        String color = (fuel > 300) ? "§b" : "§c";
        
        String message = "§fSoul Fuel: " + barStr + " " + color + hours + "h " + mins + "m remaining";
        
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
            }
