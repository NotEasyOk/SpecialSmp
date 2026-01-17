package com.noteasyok.spcialsmp.command;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.manager.CardRegistry;
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

        if (args.length == 0) {
            sender.sendMessage("§6§lSpcialSmp §7- §eAdmin Commands");
            sender.sendMessage("§8» §f/cards list");
            sender.sendMessage("§8» §f/cards give <player> <cardName>");
            sender.sendMessage("§8» §f/cards info <cardName>");
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
                if (!sender.hasPermission("spcialsmp.admin")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /cards give <player> <cardName>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }

                // Card name build karna (Support for spaces)
                String inputName = String.join(" ", slice(args, 2));
                String fullName = inputName.toLowerCase().endsWith(" card") ? inputName : inputName + " Card";
                
                // Case-insensitive check
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

                // ✅ FIX: Registry aur BaseCard ka method use karna taaki NBT Tag lag jaye
                ItemStack cardItem = foundCard.getItemStackWithLore(foundCard.getName());
                
                target.getInventory().addItem(cardItem);
                sender.sendMessage("§a§l✔ §fGiven §e" + foundCard.getName() + " §fto §b" + target.getName());
                return true;
            }
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /cards info <cardName>");
                    return true;
                }
                String inputName = String.join(" ", slice(args, 1));
                String fullName = inputName.toLowerCase().endsWith(" card") ? inputName : inputName + " Card";

                if (!CardRegistry.getCards().containsKey(fullName)) {
                    sender.sendMessage("§cCard not found!");
                    return true;
                }

                sender.sendMessage("§a§l" + fullName + " §eLore:");
                CardRegistry.getDescriptionLore(fullName).forEach(line -> sender.sendMessage("  " + line));
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("spcialsmp.admin")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }
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
        if (args.length == 1) {
            return List.of("list", "give", "info", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length >= 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("info"))) {
            return CardRegistry.getCards().keySet().stream()
                    .filter(k -> k.toLowerCase().contains(args[args.length-1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
                    }
