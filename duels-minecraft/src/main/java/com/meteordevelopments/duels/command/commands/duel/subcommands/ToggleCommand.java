package com.meteordevelopments.duels.command.commands.duel.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.data.UserData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleCommand extends BaseCommand {

    public ToggleCommand(final DuelsPlugin plugin) {
        super(plugin, "toggle", null, null, Permissions.TOGGLE, 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final UserData user = userManager.get((Player) sender);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.load-failure");
            return;
        }

        user.setRequests(!user.canRequest());
        lang.sendMessage(sender, "COMMAND.duel.toggle." + (user.canRequest() ? "enabled" : "disabled"));
    }
}
