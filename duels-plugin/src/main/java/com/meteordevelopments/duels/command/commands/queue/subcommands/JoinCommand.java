package com.meteordevelopments.duels.command.commands.queue.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.queue.Queue;
import com.meteordevelopments.duels.util.NumberUtil;
import com.meteordevelopments.duels.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class JoinCommand extends BaseCommand {

    public JoinCommand(final DuelsPlugin plugin) {
        super(plugin, "join", "join [queueName]", "Joins a queue by name.", Permissions.QUEUE, 2, true, "j");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final String queueName = StringUtil.join(args, " ", 1, args.length);

        final Queue queue = queueManager.getByName(queueName);

        if (queue == null) {
            lang.sendMessage(sender, "ERROR.queue.not-found", "name", queueName);
            return;
        }

        queueManager.queue(player, queue);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return queueManager.getQueueNames();
        }

        return null;
    }
}
