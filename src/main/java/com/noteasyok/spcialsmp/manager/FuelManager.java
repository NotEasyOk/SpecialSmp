package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class FuelManager {

    private static final HashMap<UUID, Integer> fuelCache = new HashMap<>();

    public static void startFuelTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    updateFuel(p);
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    private static void updateFuel(Player p) {
        UUID uid = p.getUniqueId();
        
        if (!fuelCache.containsKey(uid)) {
            int savedFuel = SpcialSmp.get().getPlayerDataManager().getFuel(uid);
            fuelCache.put(uid, savedFuel > 0 ? savedFuel : 86400); 
        }

        int currentFuel = fuelCache.get(uid);

        if (currentFuel > 0) {
            currentFuel--;
            fuelCache.put(uid, currentFuel);
            
            if (currentFuel % 60 == 0) {
                SpcialSmp.get().getPlayerDataManager().setFuel(uid, currentFuel);
            }
        } else {
            p.kickPlayer("§c§lSOUL DEAD! \n\n§7Aapka waqt khatam ho gaya.");
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(p.getName(), "§cSoul Fuel Empty", null, "Console");
            return;
        }

        String timeString = formatTime(currentFuel);
        String progressBar = getProgressBar(currentFuel, 86400); 
        String actionBarMsg = "§b§lSoul Fuel: §8[" + progressBar + "§8] §f" + timeString;
        
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBarMsg));
        
        if (currentFuel == 3600) { 
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
            p.sendTitle("§c§lWARNING!", "§eOnly 1 Hour Left!", 10, 70, 20);
        }
    }

    // ✅ Added for Command Check (Returns hours for withdraw check)
    public static int getFuelInHours(Player p) {
        int seconds = fuelCache.getOrDefault(p.getUniqueId(), 0);
        return seconds / 3600;
    }

    public static void addFuel(Player p, int hours) {
        int secondsToAdd = hours * 3600;
        int current = fuelCache.getOrDefault(p.getUniqueId(), 0);
        int newFuel = Math.min(current + secondsToAdd, 86400 * 7); // Max limit set to 7 days
        
        fuelCache.put(p.getUniqueId(), newFuel);
        SpcialSmp.get().getPlayerDataManager().setFuel(p.getUniqueId(), newFuel);
    }
    
    // ✅ Logic updated for Withdraw (Handles subtraction properly)
    public static void setFuel(Player p, int hours) {
        int seconds = hours * 3600;
        fuelCache.put(p.getUniqueId(), seconds);
        SpcialSmp.get().getPlayerDataManager().setFuel(p.getUniqueId(), seconds);
    }

    private static String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02dh %02dm", hours, minutes);
        } else {
            return String.format("%02dm %02ds", minutes, seconds);
        }
    }

    private static String getProgressBar(int current, int max) {
        int totalBars = 10;
        float percent = (float) current / max;
        int filledBars = (int) (totalBars * Math.min(percent, 1.0f));

        StringBuilder bar = new StringBuilder();
        String color = "§a"; 
        if (percent < 0.5) color = "§e"; 
        if (percent < 0.2) color = "§c"; 

        bar.append(color);
        for (int i = 0; i < filledBars; i++) bar.append("|");
        bar.append("§7"); 
        for (int i = 0; i < totalBars - filledBars; i++) bar.append("|");
        return bar.toString();
    }
            }
