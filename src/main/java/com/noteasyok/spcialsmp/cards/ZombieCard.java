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

public class ZombieCard implements Card {

    // player -> active zombies count
    private final Map<UUID, Integer> activeZombies = new HashMap<>();

    @Override
    public String getName() {
        return "Zombie Card";
    }

    @Override
    public void leftClick(Player player) {
        UUID id = player.getUniqueId();

        int max = SpcialSmp.get().getConfig()
                .getInt("zombie-card.max-zombies", 2);

        int duration = SpcialSmp.get().getConfig()
                .getInt("zombie-card.duration-seconds", 60);

        int used = activeZombies.getOrDefault(id, 0);

        if (used >= max) {
            player.sendMessage("§cZombie limit reached");
            return;
        }

        Zombie zombie = player.getWorld().spawn(
                player.getLocation(),
                Zombie.class
        );

        zombie.setBaby(true);
        zombie.setCustomName(player.getName() + "'s Zombie");
        zombie.setCustomNameVisible(false);
        zombie.setTarget(null);

        // Owner metadata (important)
        zombie.setMetadata(
                "owner",
                new FixedMetadataValue(SpcialSmp.get(), id.toString())
        );

        // Netherite armour
        EntityEquipment eq = zombie.getEquipment();
        if (eq != null) {
            eq.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            eq.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
            eq.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
            eq.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        }

        // increase count
        activeZombies.put(id, used + 1);

        // auto remove after duration
        Bukkit.getScheduler().runTaskLater(
                SpcialSmp.get(),
                () -> {
                    if (!zombie.isDead()) {
                        zombie.remove();
                    }
                    activeZombies.put(
                            id,
                            Math.max(0, activeZombies.get(id) - 1)
                    );
                },
                duration * 20L
        );
    }

    @Override
    public void rightClick(Player player) {
        // no power
    }

    @Override
    public void shiftRightClick(Player player) {
        // optional future power
    }
}
