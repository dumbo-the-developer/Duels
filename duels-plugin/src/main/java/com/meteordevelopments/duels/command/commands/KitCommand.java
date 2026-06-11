package com.meteordevelopments.duels.command.commands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.command.commands.kit.KiteditCommand;
import com.meteordevelopments.duels.command.commands.kit.KitresetCommand;
import com.meteordevelopments.duels.command.commands.kit.KitsaveCommand;
import com.meteordevelopments.duels.config.CommandsConfig.CommandSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Main kit command that handles /kit subcommands.
 * Usage: /kit edit <kitname> | /kit save | /kit reset [player] <kitname>
 */
public class KitCommand extends BaseCommand {

    public KitCommand(final DuelsPlugin plugin, final CommandSettings settings) {
        super(plugin, settings.getName(), "kit [edit|save|reset] [player] [kitname]", "Kit editing commands.", null, 1, false, settings.getAliasArray());
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(Permissions.KIT_EDIT) && !sender.hasPermission(Permissions.ADMIN)) {
            lang.sendMessage(sender, "ERROR.no-permission", "permission", Permissions.KIT_EDIT);
            return;
        }

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

            case "reset":
                new KitresetCommand(plugin).executeCommand(sender, args);
                break;
                
            default:
                lang.sendMessage(sender, "COMMAND.kit.unknown-subcommand", "subcommand", subCommand);
                break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("edit", "save", "reset");
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("edit")) {
            return handleTabCompletion(args[1], kitManager.getNames(false));
        }

        if (args[0].equalsIgnoreCase("reset")) {
            return new KitresetCommand(plugin).onTabComplete(sender, command, alias, args);
        }
        
        return null;
    }
}
