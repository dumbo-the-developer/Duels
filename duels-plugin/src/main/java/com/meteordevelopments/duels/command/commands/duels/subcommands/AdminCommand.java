package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class AdminCommand extends BaseCommand {

    private final StartCommand startCommand;

    public AdminCommand(final DuelsPlugin plugin, final StartCommand startCommand) {
        super(plugin, "admin", "admin start <player1> <player2> [kit] [arena]", "Admin management commands.", Permissions.ADMIN, 2, false);
        this.startCommand = startCommand;
    }

    public AdminCommand(final DuelsPlugin plugin) {
        this(plugin, new StartCommand(plugin));
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("start")) {
            final String[] shifted = new String[args.length - 1];
            System.arraycopy(args, 1, shifted, 0, args.length - 1);
            startCommand.execute(sender, label, shifted);
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.start.usage", "command", label);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            if ("start".startsWith(args[1].toLowerCase())) {
                return Collections.singletonList("start");
            }
            return Collections.emptyList();
        }

        if (args.length >= 3 && args[1].equalsIgnoreCase("start")) {
            final String[] shifted = new String[args.length - 1];
            System.arraycopy(args, 1, shifted, 0, args.length - 1);
            return startCommand.onTabComplete(sender, command, alias, shifted);
        }

        return null;
    }
}
