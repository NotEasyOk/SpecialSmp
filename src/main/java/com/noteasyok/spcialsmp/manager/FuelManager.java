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

    // Cache to store player fuel in SECONDS (Real Time calculation ke liye)
    private static final HashMap<UUID, Integer> fuelCache = new HashMap<>();

    public static void setupFuelSystem() {
        // Har 1 second (20 ticks) mein timer chalega
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
        
        // Agar cache mein nahi hai to DB se load karo
        if (!fuelCache.containsKey(uid)) {
            // Default 24 Hours = 86400 Seconds
            int savedFuel = SpcialSmp.get().getPlayerDataManager().getFuel(uid);
            fuelCache.put(uid, savedFuel > 0 ? savedFuel : 86400); 
        }

        int currentFuel = fuelCache.get(uid);

        // Fuel kam karna (Real Life 1 Second)
        if (currentFuel > 0) {
            currentFuel--;
            fuelCache.put(uid, currentFuel);
            
            // Database mein async save karo taaki lag na ho
            if (currentFuel % 60 == 0) { // Har 1 minute mein save
                SpcialSmp.get().getPlayerDataManager().setFuel(uid, currentFuel);
            }
        } else {
            // GAME OVER - BAN LOGIC
            p.kickPlayer("§c§lSOUL DEAD! \n\n§7Aapka waqt khatam ho gaya.");
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(p.getName(), "§cSoul Fuel Empty", null, "Console");
            return;
        }

        // --- ACTION BAR DISPLAY (Real Time 24h Style) ---
        String timeString = formatTime(currentFuel);
        String progressBar = getProgressBar(currentFuel, 86400); // 86400 sec = 24 hours
        
        String actionBarMsg = "§b§lSoul Fuel: §8[" + progressBar + "§8] §f" + timeString;
        
        // Player ko message bhejo (Jahan aapne white mark kiya hai)
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBarMsg));
        
        // Low Fuel Warning Sound
        if (currentFuel == 3600) { // Last 1 hour warning
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
            p.sendTitle("§c§lWARNING!", "§eOnly 1 Hour Left!", 10, 70, 20);
        }
    }

    // Helper: Fuel set karne ke liye (Potion peene par)
    public static void addFuel(Player p, int hours) {
        int secondsToAdd = hours * 3600;
        int current = fuelCache.getOrDefault(p.getUniqueId(), 0);
        int newFuel = Math.min(current + secondsToAdd, 86400); // Max 24 hours cap
        
        fuelCache.put(p.getUniqueId(), newFuel);
        SpcialSmp.get().getPlayerDataManager().setFuel(p.getUniqueId(), newFuel);
    }
    
    // Naye player ke liye set karna
    public static void setFuel(Player p, int minutes) {
        int seconds = minutes * 60;
        fuelCache.put(p.getUniqueId(), seconds);
        SpcialSmp.get().getPlayerDataManager().setFuel(p.getUniqueId(), seconds);
    }

    // --- UTILS ---

    // Seconds ko "23h 59m 30s" format mein badalna
    private static String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        // Agar 1 ghante se kam hai to Seconds dikhao, warna sirf H aur M
        if (hours > 0) {
            return String.format("%02dh %02dm", hours, minutes);
        } else {
            return String.format("%02dm %02ds", minutes, seconds); // Last moments mein seconds dikhenge
        }
    }

    // Progress Bar generator (Green to Red)
    private static String getProgressBar(int current, int max) {
        int totalBars = 10;
        float percent = (float) current / max;
        int filledBars = (int) (totalBars * percent);

        StringBuilder bar = new StringBuilder();
        
        // Color changing logic
        String color = "§a"; // Green
        if (percent < 0.5) color = "§e"; // Yellow
        if (percent < 0.2) color = "§c"; // Red

        bar.append(color);
        for (int i = 0; i < filledBars; i++) {
            bar.append("|");
        }
        bar.append("§7"); // Grey for empty
        for (int i = 0; i < totalBars - filledBars; i++) {
            bar.append("|");
        }
        return bar.toString();
    }
            }
