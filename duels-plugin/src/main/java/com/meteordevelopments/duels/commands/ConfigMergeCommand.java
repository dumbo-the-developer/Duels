package com.meteordevelopments.duels.commands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.converters.SimpleConfigConverter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class ConfigMergeCommand implements CommandExecutor, TabCompleter {

    private final DuelsPlugin plugin;

    public ConfigMergeCommand(DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("duels.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eUsage: /configmerge <config|lang|all>");
            return true;
        }

        String action = args[0].toLowerCase();
        
        try {
            switch (action) {
                case "config":
                    SimpleConfigConverter.autoMergeConfig(plugin, "config.yml");
                    sender.sendMessage("§aSuccessfully merged config.yml with defaults!");
                    break;
                    
                case "lang":
                    SimpleConfigConverter.autoMergeConfig(plugin, "lang.yml");
                    sender.sendMessage("§aSuccessfully merged lang.yml with defaults!");
                    break;
                    
                case "all":
                    SimpleConfigConverter.autoMergeConfig(plugin, "config.yml");
                    SimpleConfigConverter.autoMergeConfig(plugin, "lang.yml");
                    sender.sendMessage("§aSuccessfully merged all configuration files with defaults!");
                    break;
                    
                default:
                    sender.sendMessage("§cInvalid option! Use: config, lang, or all");
                    return true;
            }
        } catch (Exception e) {
            sender.sendMessage("§cFailed to merge configuration: " + e.getMessage());
            plugin.getLogger().warning("Config merge failed: " + e.getMessage());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("config");
            completions.add("lang");
            completions.add("all");
            return completions;
        }
        return new ArrayList<>();
    }
}
