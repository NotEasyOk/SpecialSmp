package com.noteasyok.spcialsmp.command;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.HeartManager;
import com.noteasyok.spcialsmp.manager.PlayerDataManager;
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
            p.sendMessage("§cUsage: /life withdraw <amount> or /life recipe");
            return true;
        }

        // --- WITHDRAW LOGIC ---
        if (args[0].equalsIgnoreCase("withdraw")) {
            if (args.length < 2) {
                p.sendMessage("§cUsage: /life withdraw <amount>");
                return true;
            }

            try {
                int amount = Integer.parseInt(args[1]);
                if (amount <= 0) {
                    p.sendMessage("§cInvalid amount!");
                    return true;
                }

                PlayerDataManager data = SpcialSmp.get().getPlayerDataManager();
                int currentLives = data.getLives(p.getUniqueId());

                // Safety: Player ke paas kam se kam 1 life bachni chahiye
                if (currentLives <= amount) {
                    p.sendMessage("§c§l[!] §7You cannot withdraw all your lives! You need at least 1 to stay.");
                    return true;
                }

                // Update Data & Give Item
                data.setLives(p.getUniqueId(), currentLives - amount);
                p.getInventory().addItem(HeartManager.getHeartItem(amount));
                
                p.sendMessage("§a§l✔ §7Withdrew §e" + amount + " §7lives into items!");
                HeartManager.updateActionBar(p); // Hunger bar ke upar update

            } catch (NumberFormatException e) {
                p.sendMessage("§cPlease enter a valid number.");
            }
            return true;
        }

        // --- RECIPE GUI ---
        if (args[0].equalsIgnoreCase("recipe")) {
            // HeartManager mein jo GUI wala method banaya tha use call karo
            // openRecipeGui(p); 
            return true;
        }

        return true;
    }

    // --- TAB COMPLETER ---
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("withdraw", "recipe"), new ArrayList<>());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("withdraw")) {
            return Arrays.asList("1", "2", "3"); // Suggestions
        }
        return new ArrayList<>();
    }
        }
