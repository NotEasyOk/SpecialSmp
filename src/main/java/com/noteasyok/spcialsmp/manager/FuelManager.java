package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.UUID;

public class FuelManager {

    private static final HashMap<UUID, Integer> fuelCache = new HashMap<>();
    private static final int DEFAULT_FUEL = 57599; // 15h 59m 59s

    public static boolean isSystemEnabled() {
        FileConfiguration config = SpcialSmp.get().getConfig();
        return config == null || config.getBoolean("settings.soul-fuel.enabled", true);
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
        long currentTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toEpochSecond();
        
        // --- FIX 1: Data Loading Logic ---
        if (!fuelCache.containsKey(uid)) {
            int savedFuel = SpcialSmp.get().getPlayerDataManager().getFuel(uid);
            long lastLogout = SpcialSmp.get().getPlayerDataManager().getLastLogout(uid);
            
            int finalFuel;
            if (savedFuel <= 0 && lastLogout == 0) {
                finalFuel = DEFAULT_FUEL;
            } else {
                long secondsOffline = currentTime - lastLogout;
                // Lossy conversion fix: Explicitly casting the result of calculation
                long result = (long) savedFuel - secondsOffline;
                if (result < 0) result = 0;
                finalFuel = (int) result;
            }
            
            // Limit Check
            if (finalFuel > DEFAULT_FUEL) finalFuel = DEFAULT_FUEL;
            fuelCache.put(uid, finalFuel);
        }

        int currentFuel = fuelCache.get(uid);

        if (currentFuel > 0) {
            currentFuel--;
            fuelCache.put(uid, currentFuel);
            
            // Database save every 60 seconds
            if (currentFuel % 60 == 0) {
                saveToDatabase(uid, currentFuel, currentTime);
            }
        } else {
            handleBan(p);
            return;
        }

        // Warning at 1 hour
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
                p.kickPlayer("§c§lSOUL DEAD! \n\n§7Your soul fuel has run out.");
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(p.getName(), "§cSoul Fuel Empty", null, "Console");
            });
        }
    }

    private static void saveToDatabase(UUID uid, int fuel, long time) {
        Bukkit.getScheduler().runTaskAsynchronously(SpcialSmp.get(), () -> {
            SpcialSmp.get().getPlayerDataManager().setFuel(uid, fuel);
            SpcialSmp.get().getPlayerDataManager().setLastLogout(uid, time);
        });
    }

    public static int getFuel(Player p) {
        return fuelCache.getOrDefault(p.getUniqueId(), 0);
    }

    public static void setFuel(Player p, int totalSeconds) {
        UUID uid = p.getUniqueId();
        fuelCache.put(uid, totalSeconds);
        long currentTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toEpochSecond();
        saveToDatabase(uid, totalSeconds, currentTime);
        Bukkit.getLogger().info("[FuelManager] Fuel manually set for " + p.getName() + " to " + totalSeconds + "s");
    }

    public static void addFuel(Player p, int hours) {
        if (!isSystemEnabled()) return;
        int secondsToAdd = hours * 3600;
        int current = getFuel(p); 
        int newFuel = Math.min(current + secondsToAdd, 86400 * 7); 
        setFuel(p, newFuel);
    }
    }
