package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.Kit;
import me.realized.duels.queue.QueueSign;
import me.realized.duels.util.BlockUtil;
import me.realized.duels.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeletesignCommand extends BaseCommand {

    public DeletesignCommand(final DuelsPlugin plugin) {
        super(plugin, "deletesign", null, null, 1, true, "delsign");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Sign sign = BlockUtil.getTargetBlock(player, Sign.class, 6);

        if (sign == null) {
            lang.sendMessage(sender, "ERROR.sign.not-a-sign");
            return;
        }

        final QueueSign queueSign = queueManager.remove(sign);

        if (queueSign == null) {
            lang.sendMessage(sender, "ERROR.sign.not-found");
            return;
        }

        sign.setType(Material.AIR);
        sign.update(true);

        final Location location = sign.getLocation();
        final Kit kit = queueSign.getKit();
        final String kitName = kit != null ? kit.getName() : "none";
        lang.sendMessage(sender, "COMMAND.duels.del-sign", "location", StringUtil.parse(location), "kit", kitName, "bet_amount", queueSign.getBet());
    }
}
