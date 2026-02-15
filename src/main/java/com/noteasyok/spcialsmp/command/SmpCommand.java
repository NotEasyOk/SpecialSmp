package com.noteasyok.spcialsmp.command;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SmpCommand implements CommandExecutor, org.bukkit.command.TabCompleter {

    private final SpcialSmp plugin;

    public SmpCommand(SpcialSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        // Check if sender is a player (Zaroori hai kyunki border player ki location par banega)
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command!");
            return true;
        }
        Player player = (Player) sender;

        // Permission Check
        if (!player.hasPermission("specialsmp.admin")) {
            player.sendMessage("§c§l[!] §7Insufficient authorization to execute this protocol.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§8§m---------------------------------------");
            player.sendMessage("§6§lSPECIAL SMP §7- Administrative Terminal");
            player.sendMessage("§e/smp start §8- §fInitiate the startup sequence.");
            player.sendMessage("§e/smp reload §8- §fSynchronize configuration file.");
            player.sendMessage("§8§m---------------------------------------");
            return true;
        }

        // Sub-command: START
        if (args[0].equalsIgnoreCase("start")) {
            // FIX: Naya manager nahi banana, SpcialSmp wala purana use karna hai
            plugin.getStartManager().runStartSequence(player); 
            player.sendMessage("§a§lSUCCESS §8» §fStartup sequence has been manually triggered.");
            return true;
        }

        // Sub-command: RELOAD
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            player.sendMessage("§b§lRELOAD §8» §fConfiguration matrix successfully synchronized.");
            return true;
        }

        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        java.util.List<String> completions = new java.util.ArrayList<>();
        if (args.length == 1) {
            if ("start".startsWith(args[0].toLowerCase())) completions.add("start");
            if ("reload".startsWith(args[0].toLowerCase())) completions.add("reload");
        }
        return completions;
    }
                                                }
