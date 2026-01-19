package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HerobrineCard extends BaseCard {

    // Flight ability track karne ke liye set
    private final Set<UUID> flyingPlayers = new HashSet<>();

    @Override
    public String getName() {
        return "Herobrine Card";
    }

    @Override
    public void leftClick(Player p) {
        World w = p.getWorld();
        for (int i = 0; i < 5; i++)
            w.strikeLightning(p.getLocation());
    }

    // --- RIGHT CLICK: FLIGHT LOGIC (Fixed) ---
    @Override
    public void rightClick(Player p) {
        if (flyingPlayers.contains(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED + "Flight ability is already active!");
            return;
        }

        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 1));
        p.setAllowFlight(true);
        p.setFlying(true);
        p.sendMessage(ChatColor.GREEN + "Herobrine's Flight Activated (10 Seconds)");

        flyingPlayers.add(p.getUniqueId());

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;

                if (!p.isOnline()) {
                    flyingPlayers.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }

                // Check: Card haath se hata YA Time khatam (200 ticks = 10s)
                if (!isHoldingHerobrineCard(p) || ticks >= 200) {
                    
                    if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                        p.setAllowFlight(false);
                        p.setFlying(false);
                    }

                    flyingPlayers.remove(p.getUniqueId());

                    if (ticks >= 200) {
                        p.sendMessage(ChatColor.RED + "Flight time over!");
                    } else {
                        p.sendMessage(ChatColor.RED + "Card removed! Flight disabled.");
                    }

                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    // --- SHIFT + RIGHT CLICK: GIANT + JUMP BOOST (Updated) ---
    @Override
    public void shiftRightClick(Player p) {
        World w = p.getWorld();
        long time = w.getTime();
        
        AttributeInstance scaleAttr = p.getAttribute(Attribute.GENERIC_SCALE);
        if (scaleAttr == null) return;

        boolean isDay = time < 13000 || time > 23000;

        if (isDay) {
            // GIANT MODE
            scaleAttr.setBaseValue(3.5); 
            // Add Jump Boost IV (Amplifier 3) for 20 seconds
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 400, 3)); 
            p.sendMessage(ChatColor.YELLOW + "Herobrine's Day Power: GIANT MODE (Jump Boost Active)!");
        } else {
            // TINY MODE
            scaleAttr.setBaseValue(0.3);
            p.setGlowing(true);
            p.sendMessage(ChatColor.RED + "Herobrine's Night Power: TINY MODE!");
        }

        // Timer to reset size and remove effects
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;

                // Stop if: Time over (20s), Card removed, or Offline
                if (ticks >= 400 || !isHoldingHerobrineCard(p) || !p.isOnline()) {
                    
                    // Reset Logic
                    scaleAttr.setBaseValue(1.0); // Size normal
                    p.setGlowing(false);         // Glowing off
                    p.removePotionEffect(PotionEffectType.JUMP_BOOST); // Jump Boost remove

                    if (p.isOnline()) {
                        p.sendMessage(ChatColor.GRAY + "Herobrine's power has faded.");
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    // Helper: Card Check
    private boolean isHoldingHerobrineCard(Player p) {
        var item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        
        String cleanName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return cleanName.equalsIgnoreCase("Herobrine Card");
    }
                                    }
