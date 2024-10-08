package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetlobbyCommand extends BaseCommand {

    public SetlobbyCommand(final DuelsPlugin plugin) {
        super(plugin, "setlobby", null, null, 1, true, "setspawn");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (!playerManager.setLobby((Player) sender)) {
            lang.sendMessage(sender, "ERROR.command.lobby-save-failure");
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.set-lobby");
    }
}
