package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        // Detect Paper API availability and handle teleport properly
        final Player player = (Player) sender;
        
        if (hasPaperTeleportAsync()) {
            // Use Paper's async teleport with proper completion handling
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
        } else {
            // Fallback to synchronous teleport on Spigot
            boolean success = player.teleport(location);
            if (success) {
                lang.sendMessage(sender, "COMMAND.duels.teleport", "name", name, "position", pos);
            } else {
                lang.sendMessage(sender, "ERROR.teleport.failed", "name", name, "position", pos);
            }
        }
    }
    
    /**
     * Checks if Paper's teleportAsync method is available
     * @return true if Paper API is available, false if running on Spigot
     */
    private boolean hasPaperTeleportAsync() {
        try {
            Player.class.getMethod("teleportAsync", Location.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], arenaManager.getNames());
        }

        return null;
    }
}
