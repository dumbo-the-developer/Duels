package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.util.NumberUtil;
import com.meteordevelopments.duels.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SetCommand extends BaseCommand {

    public SetCommand(final DuelsPlugin plugin) {
        super(plugin, "set", "set [name] [1:2]", "Sets the teleport position of an arena.", 3, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtil.join(args, " ", 1, args.length - 1).replace("-", " ");
        final ArenaImpl arena = arenaManager.get(name);

        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena.not-found", "name", name);
            return;
        }

        final int pos = NumberUtil.parseInt(args[args.length - 1]).orElse(arena.getPositions().size() + 1);

        if (pos <= 0 || pos > 2) {
            lang.sendMessage(sender, "ERROR.arena.invalid-position");
            return;
        }

        final Player player = (Player) sender;
        final Location location = player.getLocation().clone();
        arena.setPosition(player, pos, location);
        lang.sendMessage(sender, "COMMAND.duels.set", "position", pos, "name", name, "location", StringUtil.parse(location));
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], arenaManager.getNames());
        }

        if (args.length > 2) {
            return Arrays.asList("1", "2");
        }

        return null;
    }
}
