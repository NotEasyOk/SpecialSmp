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
    // FIX: Default fuel 23h 59m (86340 seconds)
    private static final int DEFAULT_FUEL = 86340; 

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
        long currentTime = System.currentTimeMillis() / 1000;
        
        // --- OFFLINE DRAIN LOGIC ---
        if (!fuelCache.containsKey(uid)) {
            int savedFuel = SpcialSmp.get().getPlayerDataManager().getFuel(uid);
            long lastLogout = SpcialSmp.get().getPlayerDataManager().getLastLogout(uid);
            
            if (savedFuel <= 0 && lastLogout == 0) {
                // Pehli baar join karne wala naya player
                savedFuel = DEFAULT_FUEL;
            } else if (lastLogout > 0) {
                // Purana player, offline time calculate karo
                long secondsPassed = currentTime - lastLogout;
                savedFuel = (int) (savedFuel - secondsPassed);
            }
            
            fuelCache.put(uid, Math.max(savedFuel, 0));
        }

        int currentFuel = fuelCache.get(uid);

        if (currentFuel > 0) {
            currentFuel--;
            fuelCache.put(uid, currentFuel);
            
            // Database mein save har 10 seconds mein
            if (currentFuel % 10 == 0) {
                SpcialSmp.get().getPlayerDataManager().setFuel(uid, currentFuel);
                SpcialSmp.get().getPlayerDataManager().setLastLogout(uid, currentTime);
            }
        } else {
            // Fuel khatam, ban player
            Bukkit.getScheduler().runTask(SpcialSmp.get(), () -> {
                p.kickPlayer("§c§lSOUL DEAD! \n\n§7Aapka waqt khatam ho gaya.");
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(p.getName(), "§cSoul Fuel Empty", null, "Console");
            });
            return;
        }

        // --- ACTION BAR DISPLAY ---
        String timeString = formatTime(currentFuel);
        String progressBar = getProgressBar(currentFuel, 86400); // Progress bar against 24h
        String actionBarMsg = "§b§lSoul Fuel: §8[" + progressBar + "§8] §f" + timeString;
        
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBarMsg));
        
        // Warning at 1 hour
        if (currentFuel == 3600) { 
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
            p.sendTitle("§c§lWARNING!", "§eOnly 1 Hour Left!", 10, 70, 20);
        }
    }

    public static long getFuel(Player p) {
        return fuelCache.getOrDefault(p.getUniqueId(), 0);
    }

    public static void setFuel(Player p, long totalSeconds) {
        int seconds = (int) totalSeconds;
        fuelCache.put(p.getUniqueId(), seconds);
        SpcialSmp.get().getPlayerDataManager().setFuel(p.getUniqueId(), seconds);
        SpcialSmp.get().getPlayerDataManager().setLastLogout(p.getUniqueId(), System.currentTimeMillis() / 1000);
    }

    public static void addFuel(Player p, int hours) {
        int secondsToAdd = hours * 3600;
        int current = fuelCache.getOrDefault(p.getUniqueId(), 0);
        int newFuel = Math.min(current + secondsToAdd, 86400 * 7); // Max 7 days
        
        fuelCache.put(p.getUniqueId(), newFuel);
        SpcialSmp.get().getPlayerDataManager().setFuel(p.getUniqueId(), newFuel);
        SpcialSmp.get().getPlayerDataManager().setLastLogout(p.getUniqueId(), System.currentTimeMillis() / 1000);
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
