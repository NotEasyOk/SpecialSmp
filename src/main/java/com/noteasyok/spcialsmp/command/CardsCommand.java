package com.noteasyok.spcialsmp.command;

import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

public class CardsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player p)) return true;
        if (args.length < 2) {
            p.sendMessage("/cards give <player> <CardName>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) return true;

        String cardName = args[2] + " Card";
        if (!CardRegistry.getCards().containsKey(cardName)) {
            p.sendMessage("Invalid card name");
            return true;
        }

        ItemStack card = new ItemStack(Material.PAPER);
        ItemMeta meta = card.getItemMeta();
        meta.setDisplayName(cardName);
        card.setItemMeta(meta);

        target.getInventory().addItem(card);
        p.sendMessage("Card given!");
        return true;
    }
            }
