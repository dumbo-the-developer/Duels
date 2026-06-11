package com.meteordevelopments.duelsffa.command;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class FfaCommandFallbackListener implements Listener {

    private final FfaCommand command;

    public FfaCommandFallbackListener(final FfaCommand command) {
        this.command = command;
    }

    @EventHandler
    public void on(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message == null || message.length() <= 1 || message.charAt(0) != '/') {
            return;
        }
        String raw = message.substring(1);
        String[] parts = raw.split(" ");
        if (parts.length == 0 || !parts[0].equalsIgnoreCase("ffa")) {
            return;
        }
        event.setCancelled(true);
        dispatch(event.getPlayer(), parts);
    }

    @EventHandler
    public void on(ServerCommandEvent event) {
        String commandLine = event.getCommand();
        if (commandLine == null) {
            return;
        }
        String[] parts = commandLine.split(" ");
        if (parts.length == 0 || !parts[0].equalsIgnoreCase("ffa")) {
            return;
        }
        event.setCancelled(true);
        dispatch(event.getSender(), parts);
    }

    private void dispatch(final CommandSender sender, final String[] parts) {
        if (parts.length <= 1) {
            command.execute(sender, "ffa", new String[0]);
            return;
        }
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        command.execute(sender, "ffa", args);
    }
}
