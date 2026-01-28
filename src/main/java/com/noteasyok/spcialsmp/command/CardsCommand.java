package com.noteasyok.spcialsmp.command;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import com.noteasyok.spcialsmp.manager.CardSpinner;
import com.noteasyok.spcialsmp.manager.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

        if (!sender.hasPermission("spcialsmp.admin")) {
            sender.sendMessage("§c§lERROR! §7You do not have permission to use this admin command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                sender.sendMessage("§e§lAvailable Cards:");
                CardRegistry.getCards().keySet().forEach(name -> sender.sendMessage(" §7- §f" + name));
                return true;
            }
            case "revive" -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("recipe")) {
                    if (sender instanceof Player p) {
                        openReviveRecipeGUI(p);
                    } else {
                        sender.sendMessage("This command can only be used by players.");
                    }
                    return true;
                }
                sender.sendMessage("§cUsage: /cards revive recipe");
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

                target.getInventory().addItem(foundCard.getItemStackWithLore(foundCard.getName()));
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
            default -> sendHelp(sender);
        }
        return true;
    }

    private void openReviveRecipeGUI(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "§0Revival Card Recipe");
        
        // Background slots
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        gMeta.setDisplayName(" ");
        glass.setItemMeta(gMeta);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        // Crafting Grid (Center)
        // Row 1: D C D
        inv.setItem(10, new ItemStack(Material.DIAMOND_BLOCK));
        inv.setItem(11, new ItemStack(Material.PAPER)); // Card
        inv.setItem(12, new ItemStack(Material.DIAMOND_BLOCK));
        // Row 2: T S T
        inv.setItem(19, new ItemStack(Material.TOTEM_OF_UNDYING));
        inv.setItem(20, new ItemStack(Material.NETHER_STAR));
        inv.setItem(21, new ItemStack(Material.TOTEM_OF_UNDYING));
        // Row 3: D B D
        inv.setItem(1, new ItemStack(Material.DIAMOND_BLOCK)); // Using top-down visual
        inv.setItem(2, new ItemStack(Material.BEACON));
        inv.setItem(3, new ItemStack(Material.DIAMOND_BLOCK));

        // Result Slot
        ItemStack result = new ItemStack(Material.PAPER);
        ItemMeta rMeta = result.getItemMeta();
        rMeta.setDisplayName("§d§lREVIVAL CARD");
        result.setItemMeta(rMeta);
        inv.setItem(15, result);

        p.openInventory(inv);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lSpcialSmp §7- §eAdmin Commands");
        sender.sendMessage("§8» §f/cards list");
        sender.sendMessage("§8» §f/cards give <player> <cardName/all>");
        sender.sendMessage("§8» §f/cards reroll <player>");
        sender.sendMessage("§8» §f/cards getbook <player>");
        sender.sendMessage("§8» §f/cards revive recipe");
        sender.sendMessage("§8» §f/cards reload");
    }

    private String[] slice(String[] arr, int start) {
        List<String> out = new ArrayList<>();
        for (int i = start; i < arr.length; i++) out.add(arr[i]);
        return out.toArray(new String[0]);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("spcialsmp.admin")) return new ArrayList<>();
        if (args.length == 1) return List.of("list", "give", "reroll", "getbook", "revive", "reload").stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        if (args.length == 2 && args[0].equalsIgnoreCase("revive")) return List.of("recipe");
        // ... (remaining tab complete logic)
        return new ArrayList<>();
    }
                    }
