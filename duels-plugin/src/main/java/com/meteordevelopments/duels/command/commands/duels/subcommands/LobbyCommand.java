package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;



public class LobbyCommand extends BaseCommand {

    public LobbyCommand(final DuelsPlugin plugin) {
        super(plugin, "lobby", null, null, 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        ((Player) sender).teleportAsync(playerManager.getLobby());
        lang.sendMessage(sender, "COMMAND.duels.lobby");
    }
}
