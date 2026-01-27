package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaskManager {

    /**
     * Har 24 ghante mein sabhi players ko task distribute karta hai
     */
    public static void startGlobalTaskTimer() {
        // 1728000L ticks = 24 hours
        Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), () -> {
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                // Fancy Effects
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
                p.sendTitle("§6§lNEW TASKS", "§eInventory check karo!", 10, 70, 20);
                
                // Fancy Bot Message in Chat
                p.sendMessage("§8§m-----------------------------------------");
                p.sendMessage("               §6§lSURVIVAL BOT               ");
                p.sendMessage("§7   Naye tasks distribute ho gaye hain!      ");
                p.sendMessage("§7   Ise 24h mein pura karo warna §c§lBAN§7!   ");
                p.sendMessage("§8§m-----------------------------------------");
                
                giveRandomTask(p);
            }
        }, 0L, 1728000L);
    }

    /**
     * Player ko ek random task book deta hai
     */
    public static void giveRandomTask(Player p) {
        TaskType[] tasks = TaskType.values();
        TaskType randomTask = tasks[new Random().nextInt(tasks.length)];

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§lAaj Ka Task §7(Right Click)");
            meta.setTitle("Task Book");
            meta.setAuthor("Survival Bot");
            
            // Hinglish Content
            List<String> pages = new ArrayList<>();
            pages.add("§0Hello §l" + p.getName() + ",\n\n§0Aapka aaj ka task hai:\n\n§1" + 
                    randomTask.getDescription() + "\n\n§0Ise pura karo aur §lSoul Potion §0pao warna 24h baad fuel khatam ho jayega!");
            
            meta.setPages(pages);
            book.setItemMeta(meta);
        }

        // Inventory check: Agar full hai toh niche gira do, warna inventory mein de do
        if (p.getInventory().firstEmpty() == -1) {
            p.getWorld().dropItemNaturally(p.getLocation(), book);
        } else {
            p.getInventory().addItem(book);
        }
    }

    /**
     * Reward: Soul Refill Potion create karta hai
     */
    public static ItemStack getSoulPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§b§l✨ SOUL REFILL POTION ✨");
            
            List<String> lore = new ArrayList<>();
            lore.add("§8§m-----------------------");
            lore.add("§7Aapki aatma ko shakti deta hai.");
            lore.add(" ");
            lore.add("§e§lREWARD:");
            lore.add("§f +24 Hours Soul Fuel");
            lore.add("§8§m-----------------------");
            meta.setLore(lore);
            
            // Potion Visuals
            meta.setColor(Color.AQUA);
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
            
            potion.setItemMeta(meta);
        }
        return potion;
    }
            }
