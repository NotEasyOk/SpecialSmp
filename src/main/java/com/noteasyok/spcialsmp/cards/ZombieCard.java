package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.*;

public class ZombieCard implements Card {

    private final Map<UUID, Integer> spawnCount = new HashMap<>();

    @Override
    public void leftClick(Player player) {

        UUID id = player.getUniqueId();
        int max = SpcialSmp.get().getConfig().getInt("zombie-card.max-zombies", 2);

        int used = spawnCount.getOrDefault(id, 0);
        if (used >= max) {
            player.sendMessage("Zombie limit reached");
            return;
        }

        Zombie zombie = player.getWorld().spawn(player.getLocation(), Zombie.class);
        zombie.setBaby(true);
        zombie.setCustomName(player.getName() + "'s Zombie");
        zombie.setCustomNameVisible(false);
        zombie.setTarget(null);

        EntityEquipment eq = zombie.getEquipment();
        if (eq != null) {
            eq.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            eq.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
            eq.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
            eq.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        }

        zombie.setMetadata("owner", new org.bukkit.metadata.FixedMetadataValue(
                SpcialSmp.get(), player.getUniqueId().toString()
        ));

        spawnCount.put(id, used + 1);

        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            zombie.remove();
            spawnCount.put(id, spawnCount.get(id) - 1);
        }, 20L * 10);
    }

    @Override
    public void rightClick(Player player) {}

    @Override
    public void shiftRightClick(Player player) {}
}
