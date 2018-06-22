package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.util.NumberUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class AddsignCommand extends BaseCommand {

    public AddsignCommand(final DuelsPlugin plugin) {
        super(plugin, "addsign", "addsign [kit] [bet]", "Creates a queue sign with kit and the bet if specified.", null, 3, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final BlockIterator iterator = new BlockIterator(player, 6);
        Sign sign = null;

        while (iterator.hasNext()) {
            final Block block = iterator.next();

            if (block.getState() instanceof Sign) {
                sign = (Sign) block.getState();
                break;
            }
        }

        if (sign == null) {
            player.sendMessage("not a sign!");
            return;
        }

        if (queueManager.create(sign, kitManager.get(StringUtils.join(args, " ", 1, args.length - 1)), NumberUtil.parseInt(args[args.length - 1]).orElse(0))) {
            player.sendMessage("Created a QueueSign at " + sign.getLocation());
        } else {
            player.sendMessage("A QueueSign already exists at " + sign.getLocation());
        }
    }
}
