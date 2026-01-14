package com.noteasyok.spcialsmp.cards;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class ZombieCard implements Card {

    @Override
    public String getName() {
        return "Zombie";
    }

    @Override
    public void leftClick(Player p) {
        Zombie z = p.getWorld().spawn(p.getLocation(), Zombie.class);
        z.setBaby(true);

        EntityEquipment eq = z.getEquipment();
        if (eq != null) {
            eq.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            eq.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
            eq.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
            eq.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        }

        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("spcialSmp"),
                z::remove, 200
        );
    }

    @Override
    public void rightClick(Player p) {}

    @Override
    public void shiftRightClick(Player p) {
        p.sendMessage("You turned into a zombie (visual only)");
    }
              }
