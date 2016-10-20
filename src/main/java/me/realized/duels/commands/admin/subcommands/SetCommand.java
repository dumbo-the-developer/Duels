package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.arena.Arena;
import me.realized.duels.commands.SubCommand;
import me.realized.duels.utilities.Helper;
import org.bukkit.entity.Player;

public class SetCommand extends SubCommand {

    public SetCommand() {
        super("set", "set [name] [1 | 2]", "duels.admin", "Set spawn position for arena.", 2);
    }

    @Override
    public void execute(Player sender, String[] args) {
        String name = Helper.join(args, 1, args.length - 1, " ");

        if (arenaManager.getArena(name) == null) {
            Helper.pm(sender, "Errors.arena-not-found", true);
            return;
        }

        Arena arena = arenaManager.getArena(name);

        if (!Helper.isInt(args[args.length - 1], false)) {
            Helper.pm(sender, "Errors.invalid-spawnpoint", true);
            return;
        }

        int position = Integer.parseInt(args[args.length - 1]);

        if (position < 1 || position > 2) {
            Helper.pm(sender, "Errors.invalid-spawnpoint", true);
            return;
        }

        arena.addPosition(position, sender.getLocation().clone());

        if (arenaManager.getGUI() != null) {
            arenaManager.getGUI().update(arenaManager.getArenas());
        }

        Helper.pm(sender, "Arenas.set-spawnpoint", true, "{POSITION}", position, "{NAME}", arena.getName());
    }
}
