package com.noteasyok.spcialsmp.listener;

import com.noteasyok.spcialsmp.manager.RevivalManager;
import com.noteasyok.spcialsmp.manager.FuelManager;
import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.BanEntry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RevivalListener implements Listener {

    private final String menuTitle = "§0Select Soul to Revive";

    @EventHandler
    public void onUseCard(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (item != null && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("§d§lREVIVAL CARD")) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                openRevivalMenu(p);
                e.setCancelled(true);
            }
        }
    }

    public void openRevivalMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, menuTitle);
        Set<BanEntry> bans = Bukkit.getBanList(org.bukkit.BanList.Type.NAME).getBanEntries();

        if (bans.isEmpty()) {
            p.sendMessage("§cNo souls are currently banned.");
            return;
        }

        for (BanEntry entry : bans) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§b" + entry.getTarget());
                List<String> lore = new ArrayList<>();
                lore.add("§7Reason: " + entry.getReason());
                lore.add("§eClick to Revive with 24h Fuel!");
                meta.setLore(lore);
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(entry.getTarget()));
                head.setItemMeta(meta);
            }
            inv.addItem(head);
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(menuTitle)) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() != Material.PLAYER_HEAD) return;

        Player p = (Player) e.getWhoClicked();
        String targetName = e.getCurrentItem().getItemMeta().getDisplayName().substring(2);
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();

        // 1. Unban the player
        RevivalManager.unbanPlayer(targetName);
        
        // 2. Set Fuel to 24 Hours (86400 seconds)
        // Direct PlayerDataManager use kar rahe hain taaki offline save ho jaye
        SpcialSmp.get().getPlayerDataManager().setFuel(targetUUID, 86400);
        
        // 3. Remove 1 Revival Card from hand
        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        itemInHand.setAmount(itemInHand.getAmount() - 1);
        
        p.closeInventory();
        
        // 4. Announcements
        p.sendMessage("§a§l✔ §fSuccess! §b" + targetName + " §fhas been revived with 24h fuel.");
        Bukkit.broadcastMessage("§d§lREVIVAL! §b" + targetName + " §fwas brought back to life by §e" + p.getName());
    }
            }
