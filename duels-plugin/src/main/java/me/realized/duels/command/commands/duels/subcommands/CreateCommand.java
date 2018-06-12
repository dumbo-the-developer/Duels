package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public class CreateCommand extends BaseCommand {

    public CreateCommand(final DuelsPlugin plugin) {
        super(plugin, "create", "create [name]", "Creates an arena with given name.", null, 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtils.join(args, " ", 1, args.length);

        if (arenaManager.get(name) != null) {
            // send msg
            return;
        }

        arenaManager.create(sender, name);
    }
}
