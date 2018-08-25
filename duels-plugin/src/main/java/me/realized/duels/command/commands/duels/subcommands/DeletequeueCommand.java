package me.realized.duels.command.commands.duels.subcommands;

import java.util.Arrays;
import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.Kit;
import me.realized.duels.util.NumberUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DeletequeueCommand extends BaseCommand {

    public DeletequeueCommand(final DuelsPlugin plugin) {
        super(plugin, "deletequeue", "deletequeue [bet] <kit>", "Deletes a queue.", 2, false, "delqueue", "delq");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final int bet = NumberUtil.parseInt(args[1]).orElse(0);
        String name = "";
        Kit kit = null;

        if (args.length > 2) {
            name = StringUtils.join(args, " ", 2, args.length).replace("-", " ");
            kit = kitManager.get(name);
        }

        if (config.isUseOwnInventoryEnabled()) {
            kit = null;
        } else if (kit == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
            return;
        }

        final String kitName = kit != null ? kit.getName() : "none";

        if (queueManager.remove(sender, kit, bet) == null) {
            lang.sendMessage(sender, "ERROR.queue.not-found", "bet_amount", bet, "kit", kitName);
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.delete-queue", "kit", kitName, "bet_amount", bet);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return Arrays.asList("0", "10", "50", "100", "500", "1000");
        }

        if (args.length > 2) {
            return handleTabCompletion(sender, args[2], "kit", kitManager.getKits(), Kit::getName);
        }

        return null;
    }
}
