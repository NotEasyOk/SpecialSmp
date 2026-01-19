package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZombieCard extends BaseCard {

    private final Map<UUID, Integer> active = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Zombie Card";
    }

    @Override
    public void leftClick(Player p) {
        int cooldownSec = SpcialSmp.get().getConfig().getInt("cards.zombie.cooldown", 30);
        if (!isCool(p, "spawn", cooldownSec)) return;

        int max = SpcialSmp.get().getConfig().getInt("zombie-card.max-zombies", 2);
        int time = SpcialSmp.get().getConfig().getInt("zombie-card.duration-seconds", 60);

        int count = active.getOrDefault(p.getUniqueId(), 0);
        if (count >= max) {
            p.sendMessage("§cZombie limit reached!");
            // Cooldown remove logic with underscore to match isCool
            cooldowns.remove(p.getUniqueId().toString() + "_spawn"); 
            return;
        }

        Zombie z = p.getWorld().spawn(p.getLocation(), Zombie.class);
        z.setBaby(true);
        z.setTarget(null);
        z.setMetadata("owner", new FixedMetadataValue(SpcialSmp.get(), p.getUniqueId().toString()));

        EntityEquipment eq = z.getEquipment();
        if (eq != null) {
            eq.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            eq.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
            eq.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
            eq.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        }

        active.put(p.getUniqueId(), count + 1);

        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            if (z.isValid()) z.remove();
            active.put(p.getUniqueId(), Math.max(0, active.getOrDefault(p.getUniqueId(), 1) - 1));
        }, time * 20L);
    }

    @Override public void rightClick(Player p) {}
    @Override public void shiftRightClick(Player p) {}

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
