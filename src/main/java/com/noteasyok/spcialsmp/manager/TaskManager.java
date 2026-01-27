package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class TaskManager {

    public static void startGlobalTaskTimer() {
        // Har 24 ghante mein distribution (20 ticks * 60s * 60m * 24h)
        Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), () -> {
            Bukkit.broadcastMessage("§6§l[BOT] §eNaye tasks distribute ho gaye hain! Inventory check karo.");
            for (Player p : Bukkit.getOnlinePlayers()) {
                giveRandomTask(p);
            }
        }, 0L, 1728000L);
    }

    public static void giveRandomTask(Player p) {
        TaskType[] tasks = TaskType.values();
        TaskType randomTask = tasks[new Random().nextInt(tasks.length)];

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta != null) {
            meta.setTitle("§6§lAaj Ka Task");
            meta.setAuthor("Survival Bot");
            meta.setPages("§0Hello §l" + p.getName() + ",\n\n§0Aapka aaj ka task hai:\n\n§1" + 
                    randomTask.getDescription() + "\n\n§0Ise pura karo aur §lSoul Potion §0pao warna 24h baad ban ho jaoge!");
            book.setItemMeta(meta);
        }

        // Inventory mein book de do (Agar jagah ho)
        p.getInventory().addItem(book);
    }
    
    public static ItemStack getSoulPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        ItemMeta meta = potion.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lSoul Refill Potion");
            meta.setLore(java.util.List.of("§7Ise peene se aapka Soul Fuel", "§7wapas 24 ghante tak full ho jayega."));
            potion.setItemMeta(meta);
        }
        return potion;
    }
  }
