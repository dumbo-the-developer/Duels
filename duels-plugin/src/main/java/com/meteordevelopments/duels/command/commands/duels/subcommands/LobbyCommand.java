package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;



public class LobbyCommand extends BaseCommand {

    public LobbyCommand(final DuelsPlugin plugin) {
        super(plugin, "lobby", null, null, 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        // Use Paper's async teleport with proper completion handling
        final Player player = (Player) sender;
        final Location lobbyLocation = playerManager.getLobby();
        
        player.teleportAsync(lobbyLocation).thenAccept(success -> {
            if (success) {
                lang.sendMessage(sender, "COMMAND.duels.lobby");
            } else {
                lang.sendMessage(sender, "ERROR.teleport.failed");
            }
        }).exceptionally(throwable -> {
            lang.sendMessage(sender, "ERROR.teleport.failed");
            return null;
        });
    }
}
