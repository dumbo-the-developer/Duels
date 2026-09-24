package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.core.queue.Queue;
import com.meteordevelopments.duels.util.NumberUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

public class QueueoptionsCommand extends BaseCommand {

    public QueueoptionsCommand(final DuelsPlugin plugin) {
        super(plugin, "queueoptions", "queueoptions [name] [rated:maxdifference] [value]", "Sets whether a queue is rated or its max rating difference.", 4, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Queue queue = queueManager.getByName(args[1]);

        if (queue == null) {
            lang.sendMessage(sender, "ERROR.queue.not-found-name", "name", args[1]);
            return;
        }

        final String option = args[2].toLowerCase();
        final String value = args[3].toLowerCase();

        if (option.equals("rated")) {
            if (!value.equals("true") && !value.equals("false")) {
                lang.sendMessage(sender, "ERROR.command.invalid-option", "option", value, "available_options", "true, false");
                return;
            }

            queueManager.setRated(queue, value.equals("true"));
            lang.sendMessage(sender, "COMMAND.duels.queue-options.rated", "name", queue.getName(), "rated", lang.getMessage("GENERAL." + value));
        } else if (option.equals("maxdifference")) {
            if (value.equals("-")) {
                queueManager.setMaxDifference(queue, null);
                lang.sendMessage(sender, "COMMAND.duels.queue-options.max-difference-default", "name", queue.getName(), "max_difference", config.getMaxDifference());
                return;
            }

            final OptionalInt amount = NumberUtil.parseInt(value);

            if (amount.isEmpty() || amount.getAsInt() < 1) {
                lang.sendMessage(sender, "ERROR.command.invalid-option", "option", value, "available_options", "-, 1+");
                return;
            }

            queueManager.setMaxDifference(queue, amount.getAsInt());
            lang.sendMessage(sender, "COMMAND.duels.queue-options.max-difference", "name", queue.getName(), "max_difference", amount.getAsInt());
        } else {
            lang.sendMessage(sender, "ERROR.command.invalid-option", "option", option, "available_options", "rated, maxdifference");
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], queueManager.getQueueNames());
        }

        if (args.length == 3) {
            return handleTabCompletion(args[2], Arrays.asList("rated", "maxdifference"));
        }

        if (args.length == 4) {
            if (args[2].equalsIgnoreCase("rated")) {
                return handleTabCompletion(args[3], Arrays.asList("true", "false"));
            }

            if (args[2].equalsIgnoreCase("maxdifference")) {
                return Arrays.asList("-", "100", "200", "400", "1000");
            }
        }

        return null;
    }
}
