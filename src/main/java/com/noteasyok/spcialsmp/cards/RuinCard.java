package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RuinCard extends BaseCard {

    // Cooldown track karne ke liye Map
    private final Map<String, Long> cooldowns = new HashMap<>();
    
    @Override
    public String getName() {
        return "Ruin Card";
    }
    
     @Override
public int getModelData() {
    return 7;
}

    @Override
public Material getMaterial() {
    return Material.GRAY_DYE;
}
    
    @Override
    public void leftClick(Player p) {
        // --- CONFIG COOLDOWN ---
        int cd = SpcialSmp.get().getConfig().getInt("cards.ruin.silverfish_cooldown", 20);
        if (!isCool(p, "silverfish", cd)) return;

        for (int i = 0; i < 10; i++) {
            p.getWorld().spawn(p.getLocation(), Silverfish.class);
        }
        p.sendMessage(ChatColor.GRAY + "Ruin unleashed: Silverfish spawned!");
    }

    @Override
    public void rightClick(Player p) {
        // no power
    }

    @Override
    public void shiftRightClick(Player p) {
        // --- CONFIG COOLDOWN ---
        int cd = SpcialSmp.get().getConfig().getInt("cards.ruin.poison_cooldown", 30);
        if (!isCool(p, "poison", cd)) return;

        p.getNearbyEntities(6, 6, 6).forEach(entity -> {
            if (entity instanceof Player target && !target.equals(p)) {
                target.addPotionEffect(
                        new PotionEffect(PotionEffectType.POISON, 200, 1)
                );
            }
        });
        p.sendMessage(ChatColor.DARK_GREEN + "Ruin's Poison Cloud activated!");
    }

    // --- COOLDOWN HELPER (Universal) ---
    private boolean isCool(Player p, String key, int seconds) {
        if (seconds <= 0) return true;
        long now = System.currentTimeMillis();
        
        // Map ki key String honi chahiye
        String mapKey = p.getUniqueId().toString() + "_" + key;
        
        if (cooldowns.containsKey(mapKey)) {
            long timeLeft = (cooldowns.get(mapKey) - now) / 1000;
            if (timeLeft > 0) {
                // Config se message uthayega
                String rawMsg = SpcialSmp.get().getConfig().getString("messages.cooldown-active", "§cWait %time%s");
                p.sendMessage(rawMsg.replace("%time%", String.valueOf(timeLeft)));
                return false;
            }
        }
        cooldowns.put(mapKey, now + (seconds * 1000L));
        return true;
    }
                    }
