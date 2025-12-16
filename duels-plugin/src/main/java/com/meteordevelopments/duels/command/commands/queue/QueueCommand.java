package com.meteordevelopments.duels.command.commands.queue;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.command.commands.queue.subcommands.JoinCommand;
import com.meteordevelopments.duels.command.commands.queue.subcommands.LeaveCommand;
import com.meteordevelopments.duels.config.CommandsConfig.CommandSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Objects;

public class QueueCommand extends BaseCommand {

    public QueueCommand(final DuelsPlugin plugin, final CommandSettings settings) {
        super(plugin, Objects.requireNonNull(settings, "settings").getName(), Permissions.QUEUE, true, settings.getAliasArray());
        child(
                new JoinCommand(plugin),
                new LeaveCommand(plugin)
        );
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (userManager.get(player) == null) {
            lang.sendMessage(sender, "ERROR.data.load-failure");
            return;
        }

        queueManager.getGui().open(player);
    }
}
