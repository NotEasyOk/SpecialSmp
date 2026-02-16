package com.noteasyok.spcialsmp.command;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.HeartManager;
import com.noteasyok.spcialsmp.manager.PlayerDataManager;
import com.noteasyok.spcialsmp.manager.RevivalManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LifeCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        if (args.length == 0) {
            p.sendMessage("§cUsage: /life <withdraw|recipe|revive>");
            return true;
        }

        // --- WITHDRAW: Life ko item mein badlo ---
        if (args[0].equalsIgnoreCase("withdraw")) {
            if (args.length < 2) {
                p.sendMessage("§cUsage: /life withdraw <amount>");
                return true;
            }
            try {
                int amount = Integer.parseInt(args[1]);
                PlayerDataManager data = SpcialSmp.get().getPlayerDataManager();
                int currentLives = data.getLives(p.getUniqueId());

                if (amount <= 0 || currentLives <= amount) {
                    p.sendMessage("§c§l[!] §7Invalid amount or not enough lives to keep 1!");
                    return true;
                }

                data.setLives(p.getUniqueId(), currentLives - amount);
                // HeartManager mein getHeartItem method hona chahiye physical life ke liye
                p.getInventory().addItem(HeartManager.getHeartItem(amount)); 
                
                p.sendMessage("§a§l✔ §7Withdrew §e" + amount + " §7lives!");
                HeartManager.updateActionBar(p);
            } catch (NumberFormatException e) {
                p.sendMessage("§cEnter a valid number.");
            }
            return true;
        }

        // --- RECIPE: Crafting dikhane ke liye ---
        if (args[0].equalsIgnoreCase("recipe")) {
            p.sendMessage("§d§lRECIPE §8» §fUse §e/recipe revival_card §f (Agar plugin registered hai)");
            // Yahan tum GUI open karne wala code bhi dal sakte ho
            return true;
        }

        // --- REVIVE: Banned player ko unban karne ke liye ---
        if (args[0].equalsIgnoreCase("revive")) {
            if (args.length < 2) {
                p.sendMessage("§cUsage: /life revive <player>");
                return true;
            }
            String target = args[1];
            RevivalManager.unbanPlayer(target); // RevivalManager ka pardon logic
            p.sendMessage("§a§l✔ §f" + target + " has been pardoned!");
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("withdraw", "recipe", "revive"), new ArrayList<>());
        }
        return new ArrayList<>();
    }
                        }
