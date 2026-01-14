package com.noteasyok.spcialsmp.command;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CardsCommand implements CommandExecutor, TabCompleter {

    public CardsCommand() {
        SpcialSmp.get().getCommand("cards").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("§aSpcialSmp - Cards command");
            sender.sendMessage("/cards list");
            sender.sendMessage("/cards give <player> <cardName>");
            sender.sendMessage("/cards info <cardName>");
            sender.sendMessage("/cards reload");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                sender.sendMessage("§eAvailable Cards:");
                CardRegistry.getCards().keySet().forEach(name -> sender.sendMessage(" - " + name));
                return true;
            }
            case "give" -> {
                if (!sender.hasPermission("spcialsmp.admin")) {
                    sender.sendMessage("§cNo permission");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("Usage: /cards give <player> <cardName>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("Player not found");
                    return true;
                }
                String cardName = String.join(" ", slice(args, 2));
                String fullName = cardName.endsWith(" Card") ? cardName : cardName + " Card";
                if (!CardRegistry.getCards().containsKey(fullName)) {
                    sender.sendMessage("Card not found");
                    return true;
                }
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(fullName);
                // get lore/description from registry (optional)
                meta.setLore(CardRegistry.getDescriptionLore(fullName));
                item.setItemMeta(meta);
                target.getInventory().addItem(item);
                sender.sendMessage("Given " + fullName + " to " + target.getName());
                return true;
            }
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /cards info <cardName>");
                    return true;
                }
                String cardName = String.join(" ", slice(args, 1));
                String fullName = cardName.endsWith(" Card") ? cardName : cardName + " Card";
                if (!CardRegistry.getCards().containsKey(fullName)) {
                    sender.sendMessage("Card not found");
                    return true;
                }
                sender.sendMessage("§a" + fullName + " info:");
                CardRegistry.getDescriptionLore(fullName).forEach(line -> sender.sendMessage("  " + line));
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("spcialsmp.admin")) {
                    sender.sendMessage("§cNo permission");
                    return true;
                }
                SpcialSmp.get().reloadConfig();
                sender.sendMessage("Config reloaded");
                return true;
            }
            default -> sender.sendMessage("Unknown subcommand");
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
                    .filter(n -> n.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("give")) {
            String partial = String.join(" ", slice(args, 2)).toLowerCase();
            return CardRegistry.getCards().keySet().stream()
                    .filter(k -> k.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
