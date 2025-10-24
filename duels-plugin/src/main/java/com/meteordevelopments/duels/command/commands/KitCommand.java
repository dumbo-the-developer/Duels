package com.meteordevelopments.duels.command.commands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.command.commands.kit.KiteditCommand;
import com.meteordevelopments.duels.command.commands.kit.KitsaveCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Main kit command that handles /kit subcommands.
 * Usage: /kit edit <kitname> | /kit save
 */
public class KitCommand extends BaseCommand {

    public KitCommand(DuelsPlugin plugin) {
        super(plugin, "kit", "kit [edit|save] [kitname]", "Kit editing commands.", Permissions.KIT_EDIT, 1, true);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            lang.sendMessage(sender, "COMMAND.kit.usage");
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "edit":
                new KiteditCommand(plugin).executeCommand(sender, args);
                break;
                
            case "save":
                new KitsaveCommand(plugin).executeCommand(sender, args);
                break;
                
            default:
                lang.sendMessage(sender, "COMMAND.kit.unknown-subcommand", "subcommand", subCommand);
                break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("edit", "save");
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("edit")) {
            return handleTabCompletion(args[1], kitManager.getNames(false));
        }
        
        return null;
    }
}
