package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class FuelManager {

    private static final NamespacedKey FUEL_KEY = new NamespacedKey(SpcialSmp.get(), "soul_fuel");
    private static final int MAX_FUEL = 1440; // 24 hours in minutes

    public static void startFuelTask() {
        // Har 1 minute mein fuel kam karne ka task (1200 ticks = 60 seconds)
        Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                reduceFuel(p, 1);
                updateActionBar(p);
            }
        }, 1200L, 1200L);

        // Action bar update task (har 2 second mein taki smooth dikhe)
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
            Bukkit.getScheduler().runTask(SpcialSmp.get(), () -> {
                p.kickPlayer("§c§lSOUL DEPLETED\n\n§7Aapka Soul Fuel khatam ho gaya hai.\n§eAb aapko koi aur player hi zinda kar sakta hai!");
                // Yahan aap Ban logic bhi daal sakte hain
            });
        } else {
            setFuel(p, next);
        }
    }

    public static void updateActionBar(Player p) {
        int fuel = getFuel(p);
        int hours = fuel / 60;
        int mins = fuel % 60;

        // Progress Bar Calculation
        int bars = 10;
        int filledBars = (int) ((double) fuel / MAX_FUEL * bars);
        StringBuilder barStr = new StringBuilder("§8[");
        
        for (int i = 0; i < bars; i++) {
            if (i < filledBars) barStr.append("§a┃"); // Filled part (Green)
            else barStr.append("§r┃"); // Empty part (Gray)
        }
        barStr.append("§8]");

        String color = (fuel > 300) ? "§b" : "§c"; // 5 ghante se kam pe Red ho jayega
        String message = "§fSoul Fuel: " + barStr + " " + color + hours + "h " + mins + "m remaining";
        
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
  }
