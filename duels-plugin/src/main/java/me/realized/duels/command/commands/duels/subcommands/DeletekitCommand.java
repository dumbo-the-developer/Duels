package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public class DeletekitCommand extends BaseCommand {

    public DeletekitCommand(final DuelsPlugin plugin) {
        super(plugin, "deletekit", "deletekit [name]", "Deletes a kit.", 2, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtils.join(args, " ", 1, args.length);

        if (kitManager.remove(sender, name) == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.deletekit", "name", name);
    }
}
