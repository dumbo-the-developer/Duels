package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.command.BaseCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public class ToggleCommand extends BaseCommand {

    public ToggleCommand(final DuelsPlugin plugin) {
        super(plugin, "toggle", "toggle [name]", "Enables or disables an arena.", null, 2, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Arena arena = arenaManager.get(StringUtils.join(args, " ", 1, args.length));

        if (arena == null) {
            // send message
            return;
        }

        if (arena.isUsed()) {
            // send message
            return;
        }

        arena.setDisabled(sender, !arena.isDisabled());
        sender.sendMessage("Arena '" + arena.getName() + "' is now " + (arena.isDisabled() ? "disabled" : "enabled") + "!");
    }
}
