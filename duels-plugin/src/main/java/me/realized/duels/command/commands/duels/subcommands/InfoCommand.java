package me.realized.duels.command.commands.duels.subcommands;

import java.util.stream.Collectors;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand extends BaseCommand {

    public InfoCommand(final DuelsPlugin plugin) {
        super(plugin, "info", "info [name]", "Displays information about the selected arena.", 2, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtils.join(args, " ", 1, args.length);
        final Arena arena = arenaManager.get(name);

        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena.not-found", "name", name);
            return;
        }

        final String inUse = arena.isUsed() ? "&atrue" : "&cfalse";
        final String disabled = arena.isDisabled() ? "&atrue" : "&cfalse";
        final String players = StringUtils.join(arena.getPlayers().stream().map(Player::getName).collect(Collectors.toList()), ", ");
        final String positions = StringUtils.join(arena.getPositions().values().stream().map(StringUtil::parse).collect(Collectors.toList()), ", ");
        lang.sendMessage(sender, "COMMAND.duels.info",
            "name", name, "in_use", inUse, "disabled", disabled, "players",
            !players.isEmpty() ? players : "none", "positions", !positions.isEmpty() ? positions : "none");
    }
}
