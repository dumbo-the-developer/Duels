package me.realized.duels.commands.admin;

import me.realized.duels.commands.BaseCommand;
import me.realized.duels.commands.SubCommand;
import me.realized.duels.commands.admin.subcommands.*;
import me.realized.duels.utilities.Lang;
import me.realized.duels.utilities.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DuelsCommand extends BaseCommand {

    private final List<SubCommand> commands = new ArrayList<>();

    public DuelsCommand() {
        super("duels", "duels.admin");
        commands.addAll(Arrays.asList(new EditCommand(), new CreateCommand(), new SetCommand(), new DeleteCommand(), new ListCommand(), new ToggleCommand(), new InfoCommand(), new SavekitCommand(), new DeletekitCommand(), new LoadkitCommand(), new SetlobbyCommand(), new SetitemCommand()));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            pm(sender, "&b&m------------------------------------------------");

            for (SubCommand sub : commands) {
                pm(sender, StringUtil.replaceWithArgs(Lang.ADMIN_COMMAND_USAGE.getMessage(), "{USAGE}", "/" + getName() + " " + sub.getUsage(), "{DESC}", sub.getDescription()));
            }

            pm(sender, "&b&m------------------------------------------------");
            return;
        }

        SubCommand subCommand = null;

        for (SubCommand command : commands) {
            if (args[0].equalsIgnoreCase(command.getName())) {
                subCommand = command;
                break;
            }
        }

        if (subCommand == null) {
            pm(sender, "&c'" + args[0] + "' is not a valid parent command.");
            return;
        }

        if (args.length < subCommand.length()) {
            pm(sender, "&9[Duels] " + StringUtil.replaceWithArgs(Lang.ADMIN_COMMAND_USAGE.getMessage(), "{USAGE}", "/" + getName() + " " + subCommand.getUsage(), "{DESC}", subCommand.getDescription()));
            return;
        }

        subCommand.execute(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return null;
        }

        List<String> result = new ArrayList<>();
        String input = args[0];

        for (SubCommand subCommand : commands) {
            if (input == null || input.isEmpty()) {
                result.add(subCommand.getName());
            } else if (subCommand.getName().startsWith(input)) {
                result.add(subCommand.getName());
            }
        }

        return result;
    }
}
