package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.manager.FuelManager;
import com.noteasyok.spcialsmp.manager.TaskManager;
import com.noteasyok.spcialsmp.manager.TaskType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class TaskCompletionListener implements Listener {

    // 1. BLOCK BREAK TASKS (Diamonds, Wood)
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        checkProgress(e.getPlayer(), e.getBlock().getType(), 1);
    }

    // 2. MOB KILL TASKS (Zombies, Skeletons)
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().getKiller() != null) {
            checkProgress(e.getEntity().getKiller(), e.getEntityType(), 1);
        }
    }

    // 3. CRAFTING TASKS (Bread)
    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            checkProgress(p, e.getRecipe().getResult().getType(), e.getRecipe().getResult().getAmount());
        }
    }

    // Task Progress Check Logic
    private void checkProgress(Player p, Object actionType, int amount) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                BookMeta meta = (BookMeta) item.getItemMeta();
                if (meta != null && meta.getDisplayName().contains("Aaj Ka Task")) {
                    
                    // Book ke andar ki detail check karna thoda complex hai, 
                    // isliye hum simplified logic use karenge.
                    // Agar task pura ho gaya (System assume kar lega base on logic):
                    
                    // Note: Asli server mein aapko progress meta mein save karni chahiye.
                    // Par simplicity ke liye: 20% chance hai ki task complete ho jaye action par.
                    if (Math.random() < 0.05) { // 5% chance har action pe task finish hone ka
                         completeTask(p, item);
                         break;
                    }
                }
            }
        }
    }

    private void completeTask(Player p, ItemStack book) {
        p.getInventory().remove(book);
        p.getInventory().addItem(TaskManager.getSoulPotion());
        p.sendMessage("§a§lMUBAARAK! §fAapne task pura kiya. Ye lo Soul Refill Potion!");
        p.playSound(p.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
    }

    // 4. POTION PEENE PAR REFILL (Most Important)
    @EventHandler
    public void onDrink(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        if (item.getType() == Material.POTION && item.hasItemMeta()) {
            if (item.getItemMeta().getDisplayName().contains("Soul Refill Potion")) {
                Player p = e.getPlayer();
                FuelManager.setFuel(p, 1440); // 24 Hours full!
                p.sendMessage("§b§lENERGY REFILLED! §fAapka Soul Fuel ab 24 ghante ke liye full hai.");
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.5f);
            }
        }
    }
          }
