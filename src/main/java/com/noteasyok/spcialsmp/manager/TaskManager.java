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
        // Tip: 1728000L ticks = 24 Hours.
        // Server restart par ye reset na ho, isliye hum isse har 1 hour (72000L) check kar sakte hain, 
        // par abhi ke liye aapka logic sahi hai agar server 24/7 chalta hai.
        Bukkit.getScheduler().runTaskTimer(SpcialSmp.get(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
                p.sendTitle("§6§lNEW TASKS", "§eInventory check karo!", 10, 70, 20);
                
                p.sendMessage("§8§m-----------------------------------------");
                p.sendMessage("               §6§lSURVIVAL BOT               ");
                p.sendMessage("§7   Naye tasks distribute ho gaye hain!      ");
                p.sendMessage("§7   Ise 24h mein pura karo warna §c§lBAN§7!   ");
                p.sendMessage("§8§m-----------------------------------------");
                
                giveRandomTask(p);
            }
        }, 1200L, 1728000L); // Maine 0L ko 1200L (1 min) kiya hai taaki server start hote hi lag na ho.
    }

    public static void giveRandomTask(Player p) {
        TaskType[] tasks = TaskType.values();
        TaskType randomTask = tasks[new Random().nextInt(tasks.length)];

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§lAaj Ka Task §7(Right Click)");
            meta.setTitle("Task Book");
            meta.setAuthor("Survival Bot");
            
            List<String> pages = new ArrayList<>();
            pages.add("§0Hello §l" + p.getName() + ",\n\n§0Aapka aaj ka task hai:\n\n§1" + 
                    randomTask.getDescription() + "\n\n§0Ise pura karo aur §lSoul Potion §0pao warna 24h baad fuel khatam ho jayega!");
            
            meta.setPages(pages);
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
            lore.add("§7Aapki aatma ko shakti deta hai.");
            lore.add(" ");
            lore.add("§e§lREWARD:");
            lore.add("§f +24 Hours Soul Fuel");
            lore.add("§8§m-----------------------");
            meta.setLore(lore);
            meta.setColor(Color.AQUA);
            
            // ✅ Fix for all versions
            try {
                Enchantment glow = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
                if (glow != null) meta.addEnchant(glow, 1, true);
            } catch (Exception e) {
                // Fallback for older versions
                meta.addEnchant(Enchantment.getByName("DURABILITY"), 1, true);
            }
            
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            // Dynamic flag adding for 1.20.5+ tooltips
            for (ItemFlag flag : ItemFlag.values()) {
                if (flag.name().contains("HIDE_ADDITIONAL_TOOLTIP") || flag.name().contains("HIDE_POTION_EFFECTS")) {
                    meta.addItemFlags(flag);
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
        loc.getWorld().spawnParticle(Particle.ENCHANT, loc, 3, 0.1, 0.1, 0.1, 0.1);
    }
                }
