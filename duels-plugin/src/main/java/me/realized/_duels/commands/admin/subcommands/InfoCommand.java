package me.realized._duels.commands.admin.subcommands;

import me.realized._duels.arena.Arena;
import me.realized._duels.commands.SubCommand;
import me.realized._duels.utilities.Helper;
import org.bukkit.entity.Player;

public class InfoCommand extends SubCommand {

    public InfoCommand() {
        super("info", "info [name]", "duels.admin", "Show arena information.", 2);
    }

    @Override
    public void execute(Player sender, String[] args) {
        String name = Helper.join(args, 1, args.length, " ");

        if (arenaManager.getArena(name) == null) {
            Helper.pm(sender, "Errors.arena-not-found", true);
            return;
        }

        Arena arena = arenaManager.getArena(name);
        String players = Helper.join(arena.getFormattedPlayers(), "&r, ");
        String locations = Helper.join(arena.getFormattedLocations(), "&r, ");
        Helper
            .pm(sender, "Arenas.info", true, "{NAME}", arena.getName(), "{DISABLED}", arena.isDisabled(), "{IN_USE}", arena.isUsed(), "{PLAYERS}", players, "{LOCATIONS}",
                locations);
    }
}
