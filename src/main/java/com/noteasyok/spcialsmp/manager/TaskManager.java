package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaskManager {

    public static void startGlobalTaskTimer() {
        Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), () -> {
            if (!FuelManager.isSystemEnabled()) return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
                p.sendTitle("§6§lNEW TASKS", "§eCheck your inventory!", 10, 70, 20);
                
                p.sendMessage("§8§m-----------------------------------------");
                p.sendMessage("               §6§lSURVIVAL BOT               ");
                p.sendMessage("§7   New tasks have been distributed!         ");
                p.sendMessage("§7   Complete within 24h or face a §c§lBAN§7! ");
                p.sendMessage("§8§m-----------------------------------------");
                
                giveRandomTask(p);
            }
        }, 1200L, 1728000L); 
    }

    public static void giveRandomTask(Player p) {

      if (!FuelManager.isSystemEnabled()) {
        return; 
      }
        
        TaskType[] tasks = TaskType.values();
        TaskType randomTask = tasks[new Random().nextInt(tasks.length)];

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§lAaj ka task: §e" + p.getName());
            meta.setTitle("Task Book");
            meta.setAuthor("Survival Bot");
            
            List<String> pages = new ArrayList<>();
            pages.add("§0Hello §l" + p.getName() + ",\n\n§0Your task for today is:\n\n§1" + 
                    randomTask.getDescription() + "\n\n§0Complete this to receive a §lSoul Potion §0or your fuel will expire in 24h!");
            
            meta.setPages(pages);

            List<String> lore = new ArrayList<>();
            lore.add("§7Progress: §e0/" + randomTask.getAmount());
            meta.setLore(lore);

            book.setItemMeta(meta);
        }

        if (p.getInventory().firstEmpty() == -1) {
            p.getWorld().dropItemNaturally(p.getLocation(), book);
        } else {
            p.getInventory().addItem(book);
        }
    }

    public static ItemStack getSoulPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§b§l✨ SOUL REFILL POTION ✨");
            
            List<String> lore = new ArrayList<>();
            lore.add("§8§m-----------------------");
            lore.add("§7Empowers your soul.");
            lore.add(" ");
            lore.add("§e§lREWARD:");
            lore.add("§f +24 Hours Soul Fuel");
            lore.add("§8§m-----------------------");
            meta.setLore(lore);
            meta.setColor(Color.AQUA);
            
            // ✅ Fix: 1.20+ uses UNBREAKING
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            // ✅ Fix: Safe cross-version tooltip hiding
            try {
                // For Minecraft 1.20.5+
                meta.addItemFlags(ItemFlag.valueOf("HIDE_ADDITIONAL_TOOLTIP"));
            } catch (Exception e) {
                // For Minecraft 1.20.4 and below
                try {
                    meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));
                } catch (Exception e2) {
                    // Ignore if both fail
                }
            }
            
            potion.setItemMeta(meta);
        }
        return potion;
    }

    public static void playSpinParticles(org.bukkit.entity.Entity stand) {
        Location loc = stand.getLocation().add(0, 0.5, 0);
        for (double i = 0; i <= Math.PI * 2; i += Math.PI / 8) {
            double x = Math.cos(i) * 0.6;
            double z = Math.sin(i) * 0.6;
            loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, 0, z), 1, 0, 0, 0, 0.02);
        }
    }
                }
