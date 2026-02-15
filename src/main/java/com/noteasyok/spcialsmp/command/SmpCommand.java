package com.noteasyok.spcialsmp.commands;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.StartManager;
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
        
        // Permission Check
        if (!sender.hasPermission("specialsmp.admin")) {
            sender.sendMessage("§c§l[!] §7Insufficient authorization to execute this protocol.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§8§m---------------------------------------");
            sender.sendMessage("§6§lSPECIAL SMP §7- Administrative Terminal");
            sender.sendMessage("§e/smp start §8- §fInitiate the startup sequence.");
            sender.sendMessage("§e/smp reload §8- §fSynchronize configuration file.");
            sender.sendMessage("§8§m---------------------------------------");
            return true;
        }

        // Sub-command: START
        if (args[0].equalsIgnoreCase("start")) {
            StartManager manager = new StartManager(plugin);
            manager.runStartSequence();
            sender.sendMessage("§a§lSUCCESS §8» §fStartup sequence has been manually triggered.");
            return true;
        }

        // Sub-command: RELOAD
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage("§b§lRELOAD §8» §fConfiguration matrix successfully synchronized.");
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
