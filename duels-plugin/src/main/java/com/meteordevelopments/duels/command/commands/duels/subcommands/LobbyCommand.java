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
        // Detect Paper API availability and handle teleport properly
        final Player player = (Player) sender;
        final Location lobbyLocation = playerManager.getLobby();
        
        if (hasPaperTeleportAsync()) {
            // Use Paper's async teleport with proper completion handling
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
        } else {
            // Fallback to synchronous teleport on Spigot
            boolean success = player.teleport(lobbyLocation);
            if (success) {
                lang.sendMessage(sender, "COMMAND.duels.lobby");
            } else {
                lang.sendMessage(sender, "ERROR.teleport.failed");
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
}
