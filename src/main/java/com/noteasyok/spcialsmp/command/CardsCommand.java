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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
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
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        // --- FUEL WITHDRAW LOGIC (Updated with Time Parser) ---
        if (args[0].equalsIgnoreCase("fuel") && args.length >= 3 && args[1].equalsIgnoreCase("withdraw")) {
            if (!(sender instanceof Player p)) return true;

            String timeInput = args[2].toLowerCase();
            long secondsToWithdraw = parseTimeToSeconds(timeInput);

            if (secondsToWithdraw <= 0) {
                p.sendMessage("§c§l[!] §7Invalid format! Use: 1h, 10m, or 30s");
                return true;
            }

            // FuelManager se current seconds check karo
            long currentFuelSeconds = FuelManager.getFuel(p); 
            if (currentFuelSeconds < secondsToWithdraw) {
                p.sendMessage("§c§l[!] §7Not enough fuel! You have: §e" + formatTime(currentFuelSeconds));
                return true;
            }

            FuelManager.setFuel(p, currentFuelSeconds - secondsToWithdraw);
            p.getInventory().addItem(createFuelBottle(secondsToWithdraw, timeInput));
            
            p.sendMessage("§a§l✔ §7Withdrew §e" + timeInput + " §7of Soul Fuel!");
            p.playSound(p.getLocation(), Sound.ITEM_BOTTLE_FILL, 1f, 1f);
            return true;
        }

        // --- ADMIN COMMANDS ---
        if (!sender.hasPermission("spcialsmp.admin")) {
            sender.sendMessage("§c§lERROR! §7No Permission.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                sender.sendMessage("§e§lAvailable Cards:");
                CardRegistry.getCards().keySet().forEach(name -> sender.sendMessage(" §7- §f" + name));
            }
            case "revive" -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("recipe") && sender instanceof Player p) openReviveRecipeGUI(p);
            }
            case "give" -> {
                if (args.length < 3) return true;
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) return true;
                if (args[2].equalsIgnoreCase("all")) {
                    for (BaseCard card : CardRegistry.getCards().values()) target.getInventory().addItem(card.getItemStackWithLore(card.getName()));
                    return true;
                }
                String inputName = String.join(" ", slice(args, 2));
                String fullName = inputName.toLowerCase().endsWith(" card") ? inputName : inputName + " Card";
                BaseCard foundCard = null;
                for (String key : CardRegistry.getCards().keySet()) {
                    if (key.equalsIgnoreCase(fullName)) { foundCard = CardRegistry.getCards().get(key); break; }
                }
                if (foundCard != null) target.getInventory().addItem(foundCard.getItemStackWithLore(foundCard.getName()));
            }
            case "reroll" -> {
                if (args.length >= 2 && Bukkit.getPlayer(args[1]) != null) CardSpinner.openSpinGUI(Bukkit.getPlayer(args[1]));
            }
            case "getbook" -> {
                if (args.length >= 2 && Bukkit.getPlayer(args[1]) != null) TaskManager.giveRandomTask(Bukkit.getPlayer(args[1]));
            }
            case "reload" -> {
                SpcialSmp.get().reloadConfig();
                sender.sendMessage("§aReloaded!");
            }
        }
        return true;
    }

    // --- HELPER METHODS ---

    private long parseTimeToSeconds(String input) {
        try {
            if (input.endsWith("h")) return Long.parseLong(input.replace("h", "")) * 3600;
            if (input.endsWith("m")) return Long.parseLong(input.replace("m", "")) * 60;
            if (input.endsWith("s")) return Long.parseLong(input.replace("s", ""));
            return Long.parseLong(input) * 3600; // Default hours
        } catch (Exception e) { return -1; }
    }

    private String formatTime(long seconds) {
        if (seconds >= 3600) return (seconds / 3600) + "h";
        if (seconds >= 60) return (seconds / 60) + "m";
        return seconds + "s";
    }

    private ItemStack createFuelBottle(long seconds, String label) {
        ItemStack bottle = new ItemStack(Material.POTION); // Edible Potion
        ItemMeta meta = bottle.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lSoul Fuel Extract");
            List<String> lore = new ArrayList<>();
            lore.add("§7Amount: §e" + label);
            lore.add("");
            lore.add("§fDrink this to restore fuel.");
            meta.setLore(lore);
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "fuel_seconds");
            meta.getPersistentDataContainer().set(key, PersistentDataType.LONG, seconds);
            bottle.setItemMeta(meta);
        }
        return bottle;
    }

    private void openReviveRecipeGUI(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "§0Revival Card Recipe");
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);
        inv.setItem(12, new ItemStack(Material.NETHER_STAR));
        inv.setItem(15, RevivalManager.getRevivalCard());
        p.openInventory(inv);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lSpcialSmp §eCommands");
        sender.sendMessage("§f/cards fuel withdraw <1h/10m/30s>");
    }

    private String[] slice(String[] arr, int start) {
        return Arrays.copyOfRange(arr, start, arr.length);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return StringUtil.copyPartialMatches(args[0], Arrays.asList("fuel", "give", "list", "reroll", "reload"), new ArrayList<>());
        if (args.length == 2 && args[0].equalsIgnoreCase("fuel")) return List.of("withdraw");
        if (args.length == 3 && args[1].equalsIgnoreCase("withdraw")) return List.of("1h", "30m", "60s");
        return new ArrayList<>();
    }
                }
