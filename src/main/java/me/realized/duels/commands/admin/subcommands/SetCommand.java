package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.commands.SubCommand;
import me.realized.duels.utilities.Lang;
import me.realized.duels.utilities.NumberUtil;
import me.realized.duels.utilities.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetCommand extends SubCommand {

    private final ArenaManager manager;

    public SetCommand() {
        super("set", "set [name] [1 | 2]", "Set spawn position for arena.", 2);
        this.manager = getInstance().getArenaManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String name = args[1].toLowerCase();

        if (manager.getArena(name) == null) {
            pm(sender, "&cNon-existing arena.");
            return;
        }

        Arena arena = manager.getArena(name);

        if (!NumberUtil.isInt(args[2], false)) {
            pm(sender, "&cSpawn positions must be in between range 1 - 2.");
            return;
        }

        int position = Integer.parseInt(args[2]);

        if (position < 1 || position > 2) {
            pm(sender, "&cSpawn positions must be in between range 1 - 2.");
            return;
        }

        arena.addPosition(position, player.getLocation().clone());
        pm(player, StringUtil.replaceWithArgs(Lang.ARENA_SET_POSITION.getMessage(), "{POSITION}", position, "{NAME}", arena.getName()));
    }
}
