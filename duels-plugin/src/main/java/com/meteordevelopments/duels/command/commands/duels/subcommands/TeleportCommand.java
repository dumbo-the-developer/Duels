package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;



public class TeleportCommand extends BaseCommand {

    public TeleportCommand(final DuelsPlugin plugin) {
        super(plugin, "teleport", "teleport [name] <-2>", "Teleports to an arena.", 2, true, "tp", "goto");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final boolean second = args[args.length - 1].equals("-2");
        final String name = StringUtil.join(args, " ", 1, args.length - (second ? 1 : 0)).replace("-", " ");
        final ArenaImpl arena = arenaManager.get(name);

        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena.not-found", "name", name);
            return;
        }

        if (arena.getPositions().isEmpty()) {
            lang.sendMessage(sender, "ERROR.arena.no-position-set", "name", name);
            return;
        }

        final int pos = second ? 2 : 1;
        final Location location = arena.getPosition(pos);

        if (location == null) {
            lang.sendMessage(sender, "ERROR.arena.invalid-position");
            return;
        }

        // Use Paper's async teleport with proper completion handling
        final Player player = (Player) sender;
        
        player.teleportAsync(location).thenAccept(success -> {
            if (success) {
                lang.sendMessage(sender, "COMMAND.duels.teleport", "name", name, "position", pos);
            } else {
                lang.sendMessage(sender, "ERROR.teleport.failed", "name", name, "position", pos);
            }
        }).exceptionally(throwable -> {
            lang.sendMessage(sender, "ERROR.teleport.failed", "name", name, "position", pos);
            return null;
        });
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], arenaManager.getNames());
        }

        return null;
    }
}
