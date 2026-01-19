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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HerobrineCard extends BaseCard {

    private final Set<UUID> flyingPlayers = new HashSet<>();
    private final Map<String, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Herobrine Card";
    }
    
     @Override
public int getModelData() {
    return 4;
}
    
    /* ---------------- LEFT CLICK (Lightning) ---------------- */
    @Override
    public void leftClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("card-cooldowns.Herobrine Card.left", 10);
        if (!isCool(p, "lightning", cd)) return;

        World w = p.getWorld();
        for (int i = 0; i < 5; i++)
            w.strikeLightning(p.getLocation());
    }

    /* ---------------- RIGHT CLICK (Flight) ---------------- */
    @Override
    public void rightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("card-cooldowns.Herobrine Card.right", 60);
        
        if (flyingPlayers.contains(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED + "Flight ability is already active!");
            return;
        }

        if (!isCool(p, "flight", cd)) return;

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
                if (!isHoldingHerobrineCard(p) || ticks >= 200) {
                    if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                        p.setAllowFlight(false);
                        p.setFlying(false);
                    }
                    flyingPlayers.remove(p.getUniqueId());
                    p.sendMessage(ChatColor.RED + (ticks >= 200 ? "Flight time over!" : "Card removed! Flight disabled."));
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ---------------- SHIFT + RIGHT CLICK (Giant/Tiny) ---------------- */
    @Override
    public void shiftRightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("card-cooldowns.Herobrine Card.shift_right", 180);
        if (!isCool(p, "power", cd)) return;

        World w = p.getWorld();
        long time = w.getTime();
        AttributeInstance scaleAttr = p.getAttribute(Attribute.GENERIC_SCALE);
        if (scaleAttr == null) return;

        boolean isDay = time < 13000 || time > 23000;

        if (isDay) {
            scaleAttr.setBaseValue(3.5); 
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 400, 3)); 
            p.sendMessage(ChatColor.YELLOW + "Herobrine's Day Power: GIANT MODE!");
        } else {
            scaleAttr.setBaseValue(0.3);
            p.setGlowing(true);
            p.sendMessage(ChatColor.RED + "Herobrine's Night Power: TINY MODE!");
        }

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                if (ticks >= 400 || !isHoldingHerobrineCard(p) || !p.isOnline()) {
                    scaleAttr.setBaseValue(1.0);
                    p.setGlowing(false);
                    p.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    if (p.isOnline()) p.sendMessage(ChatColor.GRAY + "Herobrine's power has faded.");
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ---------------- HELPER METHODS (FIXED) ---------------- */
    private boolean isHoldingHerobrineCard(Player p) {
        var item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String cleanName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return cleanName.equalsIgnoreCase("Herobrine Card");
    }

    private boolean isCool(Player p, String key, int seconds) {
        if (seconds <= 0) return true;
        long now = System.currentTimeMillis();
        String mapKey = p.getUniqueId().toString() + "_" + key;
        
        if (cooldowns.containsKey(mapKey)) {
            long timeLeft = (cooldowns.get(mapKey) - now) / 1000;
            if (timeLeft > 0) {
                String rawMsg = SpcialSmp.get().getConfig().getString("messages.cooldown-active", "§cWait %time%s");
                p.sendMessage(rawMsg.replace("%time%", String.valueOf(timeLeft)));
                return false;
            }
        }
        cooldowns.put(mapKey, now + (seconds * 1000L));
        return true;
    }
                          }
