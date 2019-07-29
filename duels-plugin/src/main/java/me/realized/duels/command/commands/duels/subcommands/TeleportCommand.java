package me.realized.duels.command.commands.duels.subcommands;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.command.BaseCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand extends BaseCommand {

    public TeleportCommand(final DuelsPlugin plugin) {
        super(plugin, "teleport", "teleport [name]", "Teleports to an arena.", 2, true, "tp", "goto");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtils.join(args, " ", 1, args.length).replace("-", " ");
        final Arena arena = arenaManager.get(name);

        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena.not-found", "name", name);
            return;
        }

        if (arena.getPositions().isEmpty()) {
            lang.sendMessage(sender, "ERROR.arena.no-position-set", "name", name);
            return;
        }

        ((Player) sender).teleport(arena.getPositions().values().iterator().next());
        lang.sendMessage(sender, "COMMAND.duels.teleport", "name", name);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], arenaManager.getNames());
        }

        return null;
    }
}
