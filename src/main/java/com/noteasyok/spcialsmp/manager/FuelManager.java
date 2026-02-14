package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.UUID;

public class FuelManager {

    private static final HashMap<UUID, Integer> fuelCache = new HashMap<>();
    private static final int DEFAULT_FUEL = (15*3600) + (59*60) + 59; // 15h 59m 59s

    public static boolean isSystemEnabled() {
        FileConfiguration config = SpcialSmp.get().getConfig();
        return config == null || config.getBoolean("settings.soul-fuel.enabled", true);
    }

    // Task jo har second fuel kam karega
    public static void startFuelTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isSystemEnabled()) return;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    updateFuel(p);
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    private static void updateFuel(Player p) {
        if (p.hasMetadata("time_frozen")) return;

        UUID uid = p.getUniqueId();
        
        // SIMPLE LOADING: Agar cache mein nahi hai toh database se uthao (No offline calculation)
        if (!fuelCache.containsKey(uid)) {
            int savedFuel = (int) SpcialSmp.get().getPlayerDataManager().getFuel(uid);
            
            // Agar naya player hai (0 fuel), toh use default fuel do
            if (savedFuel <= 0) {
                savedFuel = DEFAULT_FUEL;
            }
            fuelCache.put(uid, savedFuel);
        }

        int currentFuel = fuelCache.get(uid);

        if (currentFuel > 0) {
            currentFuel--; // Fuel kam ho raha hai
            fuelCache.put(uid, currentFuel);
            
            // Har 60 second mein database mein save karo
            if (currentFuel % 60 == 0) {
                saveToDatabase(uid, currentFuel);
            }
        } else {
            handleBan(p);
            return;
        }

        // 1 Hour Warning
        if (currentFuel == 3600) { 
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
            p.sendTitle("§c§lWARNING!", "§eOnly 1 Hour Left!", 10, 70, 20);
        }
    }

    private static void handleBan(Player p) {
        FileConfiguration config = SpcialSmp.get().getConfig();
        boolean banEnabled = (config != null) && config.getBoolean("settings.soul-fuel.enable-ban", true);
        
        if (banEnabled) {
            Bukkit.getScheduler().runTask(SpcialSmp.get(), () -> {
                p.kickPlayer("§c§lSOUL DEAD! \n\n§7chal nikal ban ho gaya.");
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(p.getName(), "§cSoul Fuel Empty", null, "Console");
            });
        }
    }

    private static void saveToDatabase(UUID uid, int fuel) {
        Bukkit.getScheduler().runTaskAsynchronously(SpcialSmp.get(), () -> {
            SpcialSmp.get().getPlayerDataManager().setFuel(uid, fuel);
        });
    }

    public static int getFuel(Player p) {
        return fuelCache.getOrDefault(p.getUniqueId(), 0);
    }

    // --- YE COMMANDS KE LIYE FIX HAI ---

    public static void setFuel(Player p, int totalSeconds) {
        if (!isSystemEnabled()) return;
        UUID uid = p.getUniqueId();
        
        // 1. Cache turant update karo
        fuelCache.put(uid, totalSeconds);
        
        // 2. Database update (Direct save taaki cut/add kaam kare)
        saveToDatabase(uid, totalSeconds);
        
        Bukkit.getLogger().info("[Fuel] " + p.getName() + " fuel updated to " + totalSeconds + "s");
    }

    public static void addFuel(Player p, int hours) {
        if (!isSystemEnabled()) return;
        int secondsToAdd = hours * 3600;
        int current = getFuel(p); 
        int newFuel = Math.min(current + secondsToAdd, 86400 * 7); // Max 7 Days
        setFuel(p, newFuel);
    }

    public static void removeFuel(Player p, int hours) {
        if (!isSystemEnabled()) return;
        int secondsToRemove = hours * 3600;
        int current = getFuel(p);
        int newFuel = Math.max(0, current - secondsToRemove);
        setFuel(p, newFuel);
    }
                    }
