package com.meteordevelopments.duels.command.commands.queue.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand extends BaseCommand {

    public LeaveCommand(final DuelsPlugin plugin) {
        super(plugin, "leave", null, null, Permissions.QUEUE, 1, true, "l");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (queueManager.remove((Player) sender) == null) {
            lang.sendMessage(sender, "ERROR.queue.not-in-queue");
        }
    }
}
