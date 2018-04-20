package me.realized.duels.command.commands.duels.subcommands;

import java.util.Optional;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.command.BaseCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public class ToggleCommand extends BaseCommand {

    public ToggleCommand(final DuelsPlugin plugin) {
        super(plugin, "toggle", "toggle [name]", null, 2, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Optional<Arena> result = arenaManager.get(StringUtils.join(args, " ", 1, args.length));

        if (!result.isPresent()) {
            // send msg
            return;
        }

        final Arena arena = result.get();
        arena.setDisabled(!arena.isDisabled());
        sender.sendMessage("Arena '" + arena.getName() + "' is now " + (arena.isDisabled() ? "disabled" : "enabled") + "!");
    }
}
