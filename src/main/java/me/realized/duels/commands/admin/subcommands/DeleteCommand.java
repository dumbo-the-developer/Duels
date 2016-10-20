package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.arena.Arena;
import me.realized.duels.commands.SubCommand;
import me.realized.duels.utilities.Helper;
import org.bukkit.entity.Player;

public class DeleteCommand extends SubCommand {

    public DeleteCommand() {
        super("delete", "delete [name]", "duels.admin", "Remove an arena.", 2);
    }

    @Override
    public void execute(Player sender, String[] args) {
        String name = Helper.join(args, 1, args.length, " ");

        if (arenaManager.getArena(name) == null) {
            Helper.pm(sender, "Errors.arena-not-found", true);
            return;
        }

        Arena arena = arenaManager.getArena(name);

        if (arena.isUsed()) {
            Helper.pm(sender, "Errors.arena-deletion-failed", true);
            return;
        }

        arenaManager.removeArena(arena);
        Helper.pm(sender, "Arenas.deleted", true, "{NAME}", name);
    }
}
