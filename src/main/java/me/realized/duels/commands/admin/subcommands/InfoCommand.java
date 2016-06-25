package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.commands.SubCommand;
import me.realized.duels.utilities.Helper;
import me.realized.duels.utilities.Lang;
import org.bukkit.command.CommandSender;

public class InfoCommand extends SubCommand {

    private final ArenaManager manager;

    public InfoCommand() {
        super("info", "info [name]", "Show arena information.", 2);
        this.manager = getInstance().getArenaManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String name = args[1].toLowerCase();

        if (manager.getArena(name) == null) {
            pm(sender, "&cNon-existing arena.");
            return;
        }

        Arena arena = manager.getArena(name);
        String players = Helper.join(arena.getFormattedPlayers(), "&r, ");
        String locations = Helper.join(arena.getFormattedLocations(), "&r, ");

        for (String s : Lang.ARENA_INFO.getMessages()) {
            pm(sender, Helper.replaceWithArgs(s, "{NAME}", arena.getName(), "{IN_USE}", arena.isUsed(), "{DISABLED}", arena.isDisabled(), "{PLAYERS}", players, "{LOCATIONS}", locations));
        }
    }
}
