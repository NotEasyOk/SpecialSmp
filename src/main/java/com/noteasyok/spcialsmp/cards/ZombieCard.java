package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZombieCard implements BaseCard {

    private final Map<UUID, Integer> active = new HashMap<>();

    @Override
    public String getName() {
        return "Zombie Card";
    }

    @Override
    public void leftClick(Player p) {
        int max = SpcialSmp.get().getConfig().getInt("zombie-card.max-zombies", 2);
        int time = SpcialSmp.get().getConfig().getInt("zombie-card.duration-seconds", 60);

        int count = active.getOrDefault(p.getUniqueId(), 0);
        if (count >= max) {
            p.sendMessage("Zombie limit reached");
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
}
