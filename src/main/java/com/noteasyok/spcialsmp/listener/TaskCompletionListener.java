package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.manager.FuelManager;
import com.noteasyok.spcialsmp.manager.TaskManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class TaskCompletionListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        checkTaskCompletion(e.getPlayer());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().getKiller() != null) {
            checkTaskCompletion(e.getEntity().getKiller());
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            checkTaskCompletion(p);
        }
    }

    private void checkTaskCompletion(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                BookMeta meta = (BookMeta) item.getItemMeta();
                
                // Fixed: Ab ye check karega ki book ka naam "§6§lAaj ka task:" se shuru hota hai ya nahi
                if (meta != null && meta.getDisplayName().startsWith("§6§lAaj ka task:")) {
                    // Logic: 10% chance to complete on every relevant action
                    if (Math.random() < 0.10) { 
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
        
        // Fixed: Professional English messages
        p.sendMessage("§a§lCONGRATULATIONS! §fTask completed successfully. You received a Soul Potion!");
        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
    }

    @EventHandler
    public void onDrink(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        if (item.getType() == Material.POTION && item.hasItemMeta()) {
            String displayName = item.getItemMeta().getDisplayName();
            
            // Fixed: "SOUL REFILL POTION" check logic exactly matching TaskManager
            if (displayName.contains("SOUL REFILL POTION")) {
                Player p = e.getPlayer();
                FuelManager.addFuel(p, 24); // Adds 24 hours of fuel
                
                // Fixed: Professional English message
                p.sendMessage("§b§lENERGY REFILLED! §fYour Soul Fuel has been extended by 24 hours.");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.5f);
            }
        }
    }
            }
