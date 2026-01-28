package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.manager.FuelManager;
import com.noteasyok.spcialsmp.manager.TaskManager;
import com.noteasyok.spcialsmp.manager.TaskType;
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

import java.util.ArrayList;
import java.util.List;

public class TaskCompletionListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        processTask(e.getPlayer(), e.getBlock().getType());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().getKiller() != null) {
            processTask(e.getEntity().getKiller(), e.getEntityType());
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            processTask(p, e.getRecipe().getResult().getType());
        }
    }

    private void processTask(Player p, Object actionTarget) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null || item.getType() != Material.WRITTEN_BOOK) continue;
            
            BookMeta meta = (BookMeta) item.getItemMeta();
            if (meta == null || !meta.getDisplayName().startsWith("§6§lAaj ka task:")) continue;

            // Page 1 se task description utha kar TaskType dhoondna
            String pageContent = meta.getPage(1);
            for (TaskType type : TaskType.values()) {
                if (pageContent.contains(type.getDescription()) && type.getTarget().equals(actionTarget)) {
                    updateProgress(p, item, meta, type);
                    return;
                }
            }
        }
    }

    private void updateProgress(Player p, ItemStack book, BookMeta meta, TaskType type) {
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        int count = 0;

        if (!lore.isEmpty()) {
            try {
                // Lore se current number nikalna (e.g., "Progress: 5/64")
                String line = lore.get(0).replace("§7Progress: §e", "");
                count = Integer.parseInt(line.split("/")[0]);
            } catch (Exception e) { count = 0; }
        }

        count++;

        if (count >= type.getAmount()) {
            p.getInventory().remove(book);
            p.getInventory().addItem(TaskManager.getSoulPotion());
            p.sendMessage("§a§l✔ Task Done! §7Received: Soul Potion");
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        } else {
            List<String> newLore = new ArrayList<>();
            newLore.add("§7Progress: §e" + count + "/" + type.getAmount());
            meta.setLore(newLore);
            book.setItemMeta(meta);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2f);
        }
    }

    @EventHandler
    public void onDrink(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        if (item.getType() == Material.POTION && item.hasItemMeta()) {
            String name = item.getItemMeta().getDisplayName();
            
            if (name.contains("SOUL REFILL POTION")) {
                Player p = e.getPlayer();
                // ✅ Time badhane ka logic
                FuelManager.addFuel(p, 24); 
                
                p.sendMessage("§b§l+24h §fSoul fuel refilled!");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);
            }
        }
    }
                    }
