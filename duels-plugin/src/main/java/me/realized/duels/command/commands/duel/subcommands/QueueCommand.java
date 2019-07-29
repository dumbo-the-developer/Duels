package me.realized.duels.command.commands.duel.subcommands;

import java.util.Arrays;
import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.Kit;
import me.realized.duels.queue.Queue;
import me.realized.duels.util.NumberUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueCommand extends BaseCommand {

    public QueueCommand(final DuelsPlugin plugin) {
        super(plugin, "queue", "queue [-:kit] [bet]", "Joins a queue with given kit and bet.", Permissions.QUEUE, 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (args.length == getLength()) {
            queueManager.getGui().open(player);
            return;
        }

        Kit kit = null;

        if (!args[1].equals("-")) {
            String name = StringUtils.join(args, " ", 1, args.length - (args.length > 2 ? 1 : 0)).replace("-", " ");
            kit = kitManager.get(name);

            if (kit == null) {
                lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
                return;
            }
        }

        final String kitName = kit != null ? kit.getName() : lang.getMessage("none");
        final int bet = args.length > 2 ? NumberUtil.parseInt(args[args.length - 1]).orElse(0) : 0;
        final Queue queue = queueManager.get(kit, bet);

        if (queue == null) {
            lang.sendMessage(sender, "ERROR.queue.not-found", "bet_amount", bet, "kit", kitName);
            return;
        }

        queueManager.queue(player, queue);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], kitManager.getNames());
        }

        if (args.length > 2) {
            return Arrays.asList("0", "10", "50", "100", "500", "1000");
        }

        return null;
    }
}
