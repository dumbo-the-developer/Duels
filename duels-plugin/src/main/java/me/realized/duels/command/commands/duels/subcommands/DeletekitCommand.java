package me.realized.duels.command.commands.duels.subcommands;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DeletekitCommand extends BaseCommand {

    public DeletekitCommand(final DuelsPlugin plugin) {
        super(plugin, "deletekit", "deletekit [name]", "Deletes a kit.", 2, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtil.join(args, " ", 1, args.length).replace("-", " ");

        if (kitManager.remove(sender, name) == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.delete-kit", "name", name);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], kitManager.getNames(false));
        }

        return null;
    }
}
