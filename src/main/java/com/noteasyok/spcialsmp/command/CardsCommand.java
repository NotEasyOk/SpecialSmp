package com.noteasyok.spcialsmp.command;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.cards.BaseCard;
import com.noteasyok.spcialsmp.manager.CardRegistry;
import com.noteasyok.spcialsmp.manager.CardSpinner;
import com.noteasyok.spcialsmp.manager.TaskManager;
import com.noteasyok.spcialsmp.manager.RevivalManager;
import com.noteasyok.spcialsmp.manager.FuelManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardsCommand implements CommandExecutor, TabCompleter, Listener {

    public CardsCommand() {
        if (SpcialSmp.get().getCommand("cards") != null) {
            SpcialSmp.get().getCommand("cards").setTabCompleter(this);
        }
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        // --- FUEL WITHDRAW LOGIC (Advanced Parser) ---
        if (args[0].equalsIgnoreCase("fuel") && args.length >= 3 && args[1].equalsIgnoreCase("withdraw")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("Only players can withdraw fuel!");
                return true;
            }

            String input = args[2].toLowerCase();
            long secondsToWithdraw;
            try {
                if (input.endsWith("h")) secondsToWithdraw = Long.parseLong(input.replace("h", "")) * 3600;
                else if (input.endsWith("m")) secondsToWithdraw = Long.parseLong(input.replace("m", "")) * 60;
                else if (input.endsWith("s")) secondsToWithdraw = Long.parseLong(input.replace("s", ""));
                else secondsToWithdraw = Long.parseLong(input) * 3600; // Default to hours
            } catch (NumberFormatException e) {
                p.sendMessage("§c§l[!] §7Invalid format! Use 1h, 10m, or 30s.");
                return true;
            }

            long currentFuelSec = FuelManager.getFuel(p); 
            if (currentFuelSec < secondsToWithdraw) {
                p.sendMessage("§c§l[!] §7You don't have enough Soul Fuel!");
                return true;
            }

            FuelManager.setFuel(p, currentFuelSec - secondsToWithdraw);
            p.getInventory().addItem(createFuelBottle(secondsToWithdraw, input));
            
            p.sendMessage("§a§l[!] §7Withdrew §e" + input + " §7Soul Fuel into a bottle!");
            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
            return true;
        }

        if (!sender.hasPermission("spcialsmp.admin")) {
            sender.sendMessage("§c§lERROR! §7You do not have permission to use this admin command.");
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
                    if (sender instanceof Player p) openReviveRecipeGUI(p);
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
                    if (key.equalsIgnoreCase(fullName)) { foundCard = CardRegistry.getCards().get(key); break; }
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
                if (args.length >= 2 && Bukkit.getPlayer(args[1]) != null) {
                    CardSpinner.openSpinGUI(Bukkit.getPlayer(args[1]));
                    sender.sendMessage("§a§l✔ §fStarting reroll animation for §b" + args[1]);
                }
                return true;
            }
            case "getbook" -> {
                if (args.length >= 2 && Bukkit.getPlayer(args[1]) != null) {
                    TaskManager.giveRandomTask(Bukkit.getPlayer(args[1]));
                    sender.sendMessage("§a§l✔ §fTask Book sent to §b" + args[1]);
                }
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

    private ItemStack createFuelBottle(long seconds, String label) {
        ItemStack bottle = new ItemStack(Material.POTION); 
        ItemMeta meta = bottle.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lSoul Fuel Bottle §7(§e" + label + "§7)");
            List<String> lore = new ArrayList<>();
            lore.add("§8------------------");
            lore.add("§7Drink this to claim fuel.");
            lore.add("§7Contains: §e" + label);
            lore.add("§8------------------");
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "fuel_seconds_data");
            meta.getPersistentDataContainer().set(key, PersistentDataType.LONG, seconds);
            bottle.setItemMeta(meta);
        }
        return bottle;
    }

    @EventHandler
    public void onDrink(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        if (item.getType() == Material.POTION && item.hasItemMeta()) {
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "fuel_seconds_data");
            if (item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.LONG)) {
                long seconds = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.LONG);
                FuelManager.setFuel(e.getPlayer(), FuelManager.getFuel(e.getPlayer()) + seconds);
                e.getPlayer().sendMessage("§b§l[+] §7Restored §e" + seconds + "s §7of Soul Fuel!");
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
            }
        }
    }

    private void openReviveRecipeGUI(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "§0Revival Card Recipe");
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        if (gMeta != null) { gMeta.setDisplayName(" "); glass.setItemMeta(gMeta); }
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        // --- RESTORED ORIGINAL RECIPE MATERIALS ---
        inv.setItem(2, new ItemStack(Material.DIAMOND_BLOCK));
        inv.setItem(3, new ItemStack(Material.ECHO_SHARD));
        inv.setItem(4, new ItemStack(Material.DIAMOND_BLOCK));
        inv.setItem(11, new ItemStack(Material.TOTEM_OF_UNDYING));
        inv.setItem(12, new ItemStack(Material.NETHER_STAR));
        inv.setItem(13, new ItemStack(Material.TOTEM_OF_UNDYING));
        inv.setItem(20, new ItemStack(Material.DIAMOND_BLOCK));
        inv.setItem(21, new ItemStack(Material.BEACON));
        inv.setItem(22, new ItemStack(Material.DIAMOND_BLOCK));
        inv.setItem(15, RevivalManager.getRevivalCard());
        p.openInventory(inv);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lSpcialSmp §7- §eCommands");
        sender.sendMessage("§8» §a/cards fuel withdraw <1h/10m/30s> §7(Public)");
        if (sender.hasPermission("spcialsmp.admin")) {
            sender.sendMessage("§8» §f/cards list");
            sender.sendMessage("§8» §f/cards give <player> <cardName/all>");
            sender.sendMessage("§8» §f/cards reroll <player>");
            sender.sendMessage("§8» §f/cards getbook <player>");
            sender.sendMessage("§8» §f/cards revive recipe");
            sender.sendMessage("§8» §f/cards reload");
        }
    }

    private String[] slice(String[] arr, int start) {
        return Arrays.copyOfRange(arr, start, arr.length);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("fuel"));
            if (sender.hasPermission("spcialsmp.admin")) subs.addAll(Arrays.asList("list", "give", "reroll", "getbook", "revive", "reload"));
            return StringUtil.copyPartialMatches(args[0], subs, new ArrayList<>());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("fuel")) return List.of("withdraw");
        return new ArrayList<>();
    }
                    }
