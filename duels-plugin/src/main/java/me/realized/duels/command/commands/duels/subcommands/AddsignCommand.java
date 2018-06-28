package me.realized.duels.command.commands.duels.subcommands;

import java.util.Arrays;
import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.Kit;
import me.realized.duels.util.BlockUtil;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddsignCommand extends BaseCommand {

    public AddsignCommand(final DuelsPlugin plugin) {
        super(plugin, "addsign", "addsign [kit] [bet]", "Creates a queue sign with kit and bet.", 3, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Sign sign = BlockUtil.getTargetBlock(player, Sign.class, 6);

        if (sign == null) {
            lang.sendMessage(sender, "ERROR.sign.not-a-sign");
            return;
        }

        final String name = StringUtils.join(args, " ", 1, args.length - 1).replace("-", " ");
        final Kit kit = kitManager.get(name);

        if (!config.isUseOwnInventoryEnabled() && kit == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
            return;
        }

        final int bet = NumberUtil.parseInt(args[args.length - 1]).orElse(0);

        if (!queueManager.create(sign, kit, bet)) {
            lang.sendMessage(sender, "ERROR.sign.already-exists");
            return;
        }

        final Location location = sign.getLocation();
        final String kitName = kit != null ? kit.getName() : "none";
        lang.sendMessage(sender, "COMMAND.duels.add-sign", "location", StringUtil.parse(location), "kit", kitName, "bet_amount", bet);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(sender, args[1], "kit", kitManager.getKits(), Kit::getName);
        }

        if (args.length > 2) {
            return Arrays.asList("10", "50", "100", "500", "1000");
        }

        return null;
    }
}
