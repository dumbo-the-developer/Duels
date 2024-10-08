package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.data.UserData;
import org.bukkit.command.CommandSender;

public class ResetCommand extends BaseCommand {

    public ResetCommand(final DuelsPlugin plugin) {
        super(plugin, "reset", "reset [name]", "Resets player's stats.", 2, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final UserData user = userManager.get(args[1]);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "name", args[1]);
            return;
        }

        user.reset();
        lang.sendMessage(sender, "COMMAND.duels.reset", "name", user.getName());
    }
}
