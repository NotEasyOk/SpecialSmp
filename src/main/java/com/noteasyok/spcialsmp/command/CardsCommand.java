package com.noteasyok.spcialsmp.command;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import com.noteasyok.spcialsmp.manager.CardSpinner;
import com.noteasyok.spcialsmp.manager.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CardsCommand implements CommandExecutor, TabCompleter {

    public CardsCommand() {
        if (SpcialSmp.get().getCommand("cards") != null) {
            SpcialSmp.get().getCommand("cards").setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // --- GLOBAL ADMIN SECURITY ---
        if (!sender.hasPermission("spcialsmp.admin")) {
            // Fixed: English message
            sender.sendMessage("§c§lERROR! §7You do not have permission to use this admin command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6§lSpcialSmp §7- §eAdmin Commands");
            sender.sendMessage("§8» §f/cards list");
            sender.sendMessage("§8» §f/cards give <player> <cardName/all>");
            sender.sendMessage("§8» §f/cards reroll <player>");
            sender.sendMessage("§8» §f/cards getbook <player>");
            sender.sendMessage("§8» §f/cards reload");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                sender.sendMessage("§e§lAvailable Cards:");
                CardRegistry.getCards().keySet().forEach(name -> sender.sendMessage(" §7- §f" + name));
                return true;
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
                    for (BaseCard card : CardRegistry.getCards().values()) {
                        target.getInventory().addItem(card.getItemStackWithLore(card.getName()));
                    }
                    // Fixed: English message
                    sender.sendMessage("§a§l✔ §fAll cards have been added to §b" + target.getName() + "'s §finventory.");
                    return true;
                }

                String inputName = String.join(" ", slice(args, 2));
                String fullName = inputName.toLowerCase().endsWith(" card") ? inputName : inputName + " Card";
                
                BaseCard foundCard = null;
                for (String key : CardRegistry.getCards().keySet()) {
                    if (key.equalsIgnoreCase(fullName)) {
                        foundCard = CardRegistry.getCards().get(key);
                        break;
                    }
                }

                if (foundCard == null) {
                    sender.sendMessage("§cCard '" + fullName + "' not found!");
                    return true;
                }

                ItemStack cardItem = foundCard.getItemStackWithLore(foundCard.getName());
                target.getInventory().addItem(cardItem);
                sender.sendMessage("§a§l✔ §fGiven §e" + foundCard.getName() + " §fto §b" + target.getName());
                return true;
            }

            case "reroll" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /cards reroll <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    // Fixed: English message
                    sender.sendMessage("§cPlayer is currently offline!");
                    return true;
                }

                CardSpinner.openSpinGUI(target);
                sender.sendMessage("§a§l✔ §fStarting reroll animation for §b" + target.getName());
                return true;
            }

            case "getbook" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /cards getbook <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    // Fixed: English message
                    sender.sendMessage("§cPlayer is currently offline!");
                    return true;
                }

                TaskManager.giveRandomTask(target);
                sender.sendMessage("§a§l✔ §fTask Book sent to §b" + target.getName());
                return true;
            }

            case "reload" -> {
                SpcialSmp.get().reloadConfig();
                sender.sendMessage("§aConfig reloaded successfully!");
                return true;
            }
            default -> sender.sendMessage("§cUnknown subcommand!");
        }

        return true;
    }

    private String[] slice(String[] arr, int start) {
        List<String> out = new ArrayList<>();
        for (int i = start; i < arr.length; i++) out.add(arr[i]);
        return out.toArray(new String[0]);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("spcialsmp.admin")) return new ArrayList<>();

        if (args.length == 1) {
            return List.of("list", "give", "reroll", "getbook", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("reroll") || args[0].equalsIgnoreCase("getbook"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length >= 3 && args[0].equalsIgnoreCase("give")) {
            List<String> options = new ArrayList<>(CardRegistry.getCards().keySet());
            options.add("all");
            return options.stream()
                    .filter(k -> k.toLowerCase().contains(args[args.length-1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
                        }
