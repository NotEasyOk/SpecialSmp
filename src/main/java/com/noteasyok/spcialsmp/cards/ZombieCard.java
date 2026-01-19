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
    // Step 1: Cooldown track karne ke liye Map
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Zombie Card";
    }

    @Override
    public void leftClick(Player p) {
        // --- COOLDOWN CHECK START ---
        // Config path: cards.zombie.cooldown
        int cooldownSec = SpcialSmp.get().getConfig().getInt("cards.zombie.cooldown", 30);
        if (!isCool(p, "spawn", cooldownSec)) return;
        // --- COOLDOWN CHECK END ---

        int max = SpcialSmp.get().getConfig().getInt("zombie-card.max-zombies", 2);
        int time = SpcialSmp.get().getConfig().getInt("zombie-card.duration-seconds", 60);

        int count = active.getOrDefault(p.getUniqueId(), 0);
        if (count >= max) {
            p.sendMessage("Zombie limit reached");
            // Agar limit reached hai, to cooldown reset kar dete hain taaki click waste na ho
            cooldowns.remove(p.getUniqueId() + "spawn"); 
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
            z.remove();
            active.put(p.getUniqueId(),
                    Math.max(0, active.get(p.getUniqueId()) - 1));
        }, time * 20L);
    }

    @Override public void rightClick(Player p) {}
    @Override public void shiftRightClick(Player p) {}

    // Step 2: Cooldown Helper Method (Universal)
    private boolean isCool(Player p, String key, int seconds) {
        long now = System.currentTimeMillis();
        String mapKey = p.getUniqueId().toString() + key;
        if (cooldowns.containsKey(mapKey)) {
            long timeLeft = (cooldowns.get(mapKey) - now) / 1000;
            if (timeLeft > 0) {
                p.sendMessage(ChatColor.RED + "Wait " + timeLeft + "s to spawn again!");
                return false;
            }
        }
        cooldowns.put(mapKey, now + (seconds * 1000L));
        return true;
    }
                             }
