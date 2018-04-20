package me.realized.duels.command.commands.duels.subcommands;

import java.util.Optional;
import java.util.OptionalLong;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.util.NumberUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetCommand extends BaseCommand {

    public SetCommand(final DuelsPlugin plugin) {
        super(plugin, "set", "set [name] [1 | 2]", null, 3, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Optional<Arena> result = arenaManager.get(StringUtils.join(args, " ", 1, args.length - 1));

        if (!result.isPresent()) {
            // send msg
            return;
        }

        final OptionalLong pos = NumberUtil.parseLong(args[args.length - 1]);

        if (!pos.isPresent()) {
            // send msg
            return;
        }

        final Arena arena = result.get();
        arena.setPosition((int) pos.getAsLong(), ((Player) sender).getLocation().clone());
        sender.sendMessage("Set pos " + pos.getAsLong() + " for arena '" + arena.getName() + "'!");
    }
}
