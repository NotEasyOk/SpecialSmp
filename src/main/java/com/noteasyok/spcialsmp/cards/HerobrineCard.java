package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class HerobrineCard extends BaseCard {

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

    @Override
    public void rightClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 1));
        p.setAllowFlight(true);
    }

    @Override
    public void shiftRightClick(Player p) {
        World w = p.getWorld();
        long time = w.getTime();
        
        // 1.21 Scale Attribute lena
        AttributeInstance scaleAttr = p.getAttribute(Attribute.GENERIC_SCALE);
        if (scaleAttr == null) return;

        // Day vs Night check
        boolean isDay = time < 13000 || time > 23000;

        if (isDay) {
            scaleAttr.setBaseValue(3.5); // Giant (3.5x size)
            p.sendMessage(ChatColor.YELLOW + "Herobrine's Day Power: GIANT MODE!");
        } else {
            scaleAttr.setBaseValue(0.3); // Tiny (0.3x size)
            p.setGlowing(true);
            p.sendMessage(ChatColor.RED + "Herobrine's Night Power: TINY MODE!");
        }

        // Timer and Hand Check (20 Seconds = 400 ticks)
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;

                // 1. Agar 20 second khatam ho gaye
                // 2. Agar player ne card haat se hata diya
                // 3. Agar player offline chala gaya
                if (ticks >= 400 || !isHoldingHerobrineCard(p) || !p.isOnline()) {
                    scaleAttr.setBaseValue(1.0); // Reset to Normal
                    p.setGlowing(false);
                    
                    if (p.isOnline()) {
                        p.sendMessage(ChatColor.GRAY + "Herobrine's power has faded.");
                    }
                    this.cancel();
                    return;
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    // Hand check helper
    private boolean isHoldingHerobrineCard(Player p) {
        var item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        
        String cleanName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return cleanName.equalsIgnoreCase("Herobrine Card");
    }
            }
