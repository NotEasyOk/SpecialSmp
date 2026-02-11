package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.UUID;

public class FuelManager {

    private static final HashMap<UUID, Integer> fuelCache = new HashMap<>();
    private static final int DEFAULT_FUEL = 57599; 

    public static boolean isSystemEnabled() {
        FileConfiguration config = SpcialSmp.get().getConfig();
        if (config == null) return true; 
        return config.getBoolean("settings.soul-fuel.enabled", true);
    }

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
    long currentTime = System.currentTimeMillis() / 1000;
    
    if (!fuelCache.containsKey(uid)) {
        int savedFuel = SpcialSmp.get().getPlayerDataManager().getFuel(uid);
        long lastLogout = SpcialSmp.get().getPlayerDataManager().getLastLogout(uid);
        
        // --- OFFLINE DRAIN LOGIC ---
        if (lastLogout > 0) {
            long secondsOffline = currentTime - lastLogout;
            savedFuel = Math.max(0, savedFuel - (int) secondsOffline);
        }
        
        fuelCache.put(uid, savedFuel);
    }

    int currentFuel = fuelCache.get(uid);

    if (currentFuel > 0) {
        currentFuel--;
        fuelCache.put(uid, currentFuel);
        
        // Har 1 minute mein file update karo taaki crash hone par data na jaye
        if (currentFuel % 60 == 0) {
            saveToDatabase(uid, currentFuel, currentTime);
        }
    } else {
            FileConfiguration config = SpcialSmp.get().getConfig();
            boolean banEnabled = (config != null) && config.getBoolean("settings.soul-fuel.enable-ban", true);
            
            if (banEnabled) {
                Bukkit.getScheduler().runTask(SpcialSmp.get(), () -> {
                    p.kickPlayer("§c§lSOUL DEAD! \n\n§7chal nikal ban ho gaya.");
                    Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(p.getName(), "§cSoul Fuel Empty", null, "Console");
                });
            }
            return;
        }

        if (currentFuel == 3600) { 
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
            p.sendTitle("§c§lWARNING!", "§eOnly 1 Hour Left!", 10, 70, 20);
        }
    }

    private static void saveToDatabase(UUID uid, int fuel, long time) {
        // Auto-save happens in background
        Bukkit.getScheduler().runTaskAsynchronously(SpcialSmp.get(), () -> {
            SpcialSmp.get().getPlayerDataManager().setFuel(uid, fuel);
            SpcialSmp.get().getPlayerDataManager().setLastLogout(uid, time);
        });
    }

    public static int getFuel(Player p) {
        return fuelCache.getOrDefault(p.getUniqueId(), 0);
    }

    public static void setFuel(Player p, long totalSeconds) {
        setFuel(p, (int) totalSeconds);
    }

    // FIXED: Synchronized setFuel to prevent overwrite
    public static void setFuel(Player p, int totalSeconds) {
        if (!isSystemEnabled()) {
            p.sendMessage("§cLife System is currently disabled!");
            return;
        }
        UUID uid = p.getUniqueId();
        
        // 1. Update RAM immediately
        fuelCache.put(uid, totalSeconds);
        
        // 2. Update File immediately (No Async here)
        // This stops the "Fuel not cutting" bug
        SpcialSmp.get().getPlayerDataManager().setFuel(uid, totalSeconds);
        SpcialSmp.get().getPlayerDataManager().setLastLogout(uid, System.currentTimeMillis() / 1000);
    }

    public static void addFuel(Player p, int hours) {
        if (!isSystemEnabled()) return;
        int secondsToAdd = hours * 3600;
        int current = getFuel(p); 
        int newFuel = Math.min(current + secondsToAdd, 86400 * 7); 
        setFuel(p, newFuel);
    }
    }
