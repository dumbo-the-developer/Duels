package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.queue.Queue;
import com.meteordevelopments.duels.queue.sign.QueueSignImpl;
import com.meteordevelopments.duels.util.BlockUtil;
import com.meteordevelopments.duels.util.StringUtil;
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

        final QueueSignImpl queueSign = queueSignManager.remove(player, sign.getLocation());

        if (queueSign == null) {
            lang.sendMessage(sender, "ERROR.sign.not-found");
            return;
        }

        sign.setType(Material.AIR);
        sign.update(true);

        final Location location = sign.getLocation();
        final Queue queue = queueSign.getQueue();
        final Kit kit = queue.getKit();
        final String kitName = kit != null ? kit.getName() : lang.getMessage("GENERAL.none");
        lang.sendMessage(sender, "COMMAND.duels.del-sign", "location", StringUtil.parse(location), "kit", kitName, "bet_amount", queue.getBet());
    }
}
