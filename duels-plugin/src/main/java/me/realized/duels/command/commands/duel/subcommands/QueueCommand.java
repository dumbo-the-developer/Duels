package me.realized.duels.command.commands.duel.subcommands;

import java.util.Arrays;
import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.extra.Permissions;
import me.realized.duels.kit.Kit;
import me.realized.duels.queue.Queue;
import me.realized.duels.util.NumberUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueCommand extends BaseCommand {

    public QueueCommand(final DuelsPlugin plugin) {
        super(plugin, "queue", "queue [kit] <bet>", "Joins a queue with given kit and bet.", Permissions.QUEUE, 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (args.length == getLength()) {
            queueManager.getGui().open(player);
            return;
        }

        final Queue queue;
        String name = "";
        int bet;

        if (config.isUseOwnInventoryEnabled()) {
            queue = queueManager.get(null, bet = NumberUtil.parseInt(args[args.length - 1]).orElse(0));
        } else {
            bet = 0;
            int max = args.length;

            if (args.length > getLength()) {
                bet = NumberUtil.parseInt(args[args.length - 1]).orElse(-1);
                max = args.length - 1;

                if (bet == -1) {
                    bet = 0;
                    max = args.length;
                }
            }

            name = StringUtils.join(args, " ", 1, max).replace("-", " ");
            final Kit kit = kitManager.get(name);

            if (kit == null) {
                lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
                return;
            }

            queue = queueManager.get(kit, bet);
        }

        if (queue == null) {
            lang.sendMessage(sender, "ERROR.queue.not-found", "bet_amount", bet, "kit", name.isEmpty() ? "none" : name);
            return;
        }

        queueManager.queue(player, queue);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(sender, args[1], "kit", kitManager.getKits(), Kit::getName);
        }

        if (args.length > 2) {
            return Arrays.asList("0", "10", "50", "100", "500", "1000");
        }

        return null;
    }
}
