package me.realized.duels.command.commands.duels.subcommands;

import java.util.OptionalInt;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.util.NumberUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetCommand extends BaseCommand {

    public SetCommand(final DuelsPlugin plugin) {
        super(plugin, "set", "set [name] [1 | 2]", "Sets the teleport location of an arena.", null, 3, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Arena arena = arenaManager.get(StringUtils.join(args, " ", 1, args.length - 1));

        if (arena == null) {
            // send msg
            return;
        }

        final OptionalInt pos = NumberUtil.parseInt(args[args.length - 1]);

        if (!pos.isPresent()) {
            // send msg
            return;
        }

        final Player player = (Player) sender;
        arena.setPosition(player, pos.getAsInt(), player.getLocation().clone());
        sender.sendMessage("Set pos " + pos.getAsInt() + " for arena '" + arena.getName() + "'!");
    }
}
