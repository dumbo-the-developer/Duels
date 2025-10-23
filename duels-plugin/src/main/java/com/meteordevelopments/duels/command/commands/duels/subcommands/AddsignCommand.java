package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.queue.Queue;
import com.meteordevelopments.duels.util.BlockUtil;
import com.meteordevelopments.duels.util.NumberUtil;
import com.meteordevelopments.duels.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class AddsignCommand extends BaseCommand {

    public AddsignCommand(final DuelsPlugin plugin) {
        super(plugin, "addsign", "addsign [queueName] [bet] [size] [kit:-]", "Creates a queue sign with given queue name, bet, team size and kit.", 5, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Sign sign = BlockUtil.getTargetBlock(player, Sign.class, 6);

        if (sign == null) {
            lang.sendMessage(sender, "ERROR.sign.not-a-sign");
            return;
        }

        final String queueName = args[1];
        final int bet = NumberUtil.parseInt(args[2]).orElse(0);
        final int size = Math.max(1, NumberUtil.parseInt(args[3]).orElse(1));
        KitImpl kit = null;

        if (!args[4].equals("-")) {
            String kitName = StringUtil.join(args, " ", 4, args.length).replace("-", " ");
            kit = kitManager.get(kitName);

            if (kit == null) {
                lang.sendMessage(sender, "ERROR.kit.not-found", "name", kitName);
                return;
            }
        }

        final String kitName = kit != null ? kit.getName() : lang.getMessage("GENERAL.none");
        Queue queue = queueManager.getByName(queueName);

        if (queue == null) {
            // Create queue with name, team size if it doesn't exist
            queueManager.create(player, queueName, kit, bet, size);
            queue = queueManager.getByName(queueName);
            if (queue == null) {
                lang.sendMessage(sender, "ERROR.queue.create-failed", "name", queueName, "kit", kitName, "bet_amount", bet);
                return;
            }
        }

        if (!queueSignManager.create(player, sign.getLocation(), queue)) {
            lang.sendMessage(sender, "ERROR.sign.already-exists");
            return;
        }

        final Location location = sign.getLocation();
        lang.sendMessage(sender, "COMMAND.duels.add-sign", "location", StringUtil.parse(location), "name", queueName, "kit", kitName, "bet_amount", bet);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return Arrays.asList("Ranked", "Casual", "Practice", "Tournament");
        }

        if (args.length == 3) {
            return Arrays.asList("0", "10", "50", "100", "500", "1000");
        }

        if (args.length == 4) {
            return Arrays.asList("1", "2", "3", "4", "5");
        }

        if (args.length > 4) {
            return handleTabCompletion(args[4], kitManager.getNames(true));
        }

        return null;
    }
}
