package com.noteasyok.spcialsmp.command;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import com.noteasyok.spcialsmp.manager.CardSpinner;
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

public class CardsCommand implements CommandExecutor, TabCompleter {

    public CardsCommand() {
        if (SpcialSmp.get().getCommand("cards") != null) {
            SpcialSmp.get().getCommand("cards").setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("spcialsmp.admin")) {
            sender.sendMessage("§c§lERROR! §7You do not have permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
              sender.sendMessage("§e§lAvailable Cards:");
             CardRegistry.getCards().values().forEach(card -> {
              String status = card.isEnabled() ? "§a✔" : "§c✘";
              sender.sendMessage(" " + status + " §7- §f" + card.getName());
            });
         }
            case "give" -> {
    if (args.length < 3) {
        sender.sendMessage("§cUsage: /cards give <player> <cardName/all>");
        return true;
    }
    Player target = Bukkit.getPlayer(args[1]);
    if (target == null) {
        sender.sendMessage("§cPlayer not found!");
        return true;
    }
    if (args[2].equalsIgnoreCase("all")) {
        // CHANGE 1: sirf enabled cards do
        CardRegistry.getEnabledCards().forEach(card -> 
            target.getInventory().addItem(card.getItemStackWithLore(card.getName())));
        sender.sendMessage("§a§l✔ §fAll enabled cards given to §b" + target.getName());
        return true;
    }
    String cardName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
    BaseCard foundCard = CardRegistry.getCards().get(cardName);
    if (foundCard == null) {
        sender.sendMessage("§cCard not found!");
        return true;
    }
    // CHANGE 2: disabled card check
    if (!foundCard.isEnabled()) {
        sender.sendMessage("§cYeh card disabled hai (cards.yml mein off)!");
        return true;
    }
    target.getInventory().addItem(foundCard.getItemStackWithLore(foundCard.getName()));
    sender.sendMessage("§a§l✔ §fGiven §e" + foundCard.getName() + " §fto §b" + target.getName());
}
            case "reroll" -> {
                if (args.length >= 2) {
                    Player t = Bukkit.getPlayer(args[1]);
                    if (t != null) CardSpinner.openSpinGUI(t);
                }
            }
            case "reload" -> {
                SpcialSmp.get().reloadConfig();
                sender.sendMessage("§aConfig reloaded!");
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lCards Admin §7- §eCommands");
        sender.sendMessage("§8» §f/cards list");
        sender.sendMessage("§8» §f/cards give <player> <card/all>");
        sender.sendMessage("§8» §f/cards reroll <player>");
        sender.sendMessage("§8» §f/cards reload");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Line 83: Sub-commands (list, give, reload, etc.)
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("list", "give", "reroll", "reload"), new ArrayList<>());
        }
        
        // FIX: Player names suggestion (Line 87-91)
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("reroll"))) {
            List<String> players = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> players.add(p.getName()));
            return StringUtil.copyPartialMatches(args[1], players, new ArrayList<>());
        }

        // Line 93: Card names suggestion
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> cards = new ArrayList<>(CardRegistry.getCards().keySet());
            cards.add("all");
            return StringUtil.copyPartialMatches(args[2], cards, new ArrayList<>());
        }
        return new ArrayList<>();
   }
       }
