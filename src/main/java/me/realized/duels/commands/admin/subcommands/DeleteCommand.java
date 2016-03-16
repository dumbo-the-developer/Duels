package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.commands.SubCommand;
import me.realized.duels.utilities.Lang;
import org.bukkit.command.CommandSender;

public class DeleteCommand extends SubCommand {

    private final ArenaManager manager;

    public DeleteCommand() {
        super("delete", "delete [name]", "Remove an arena.", 2);
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

        if (arena.isUsed()) {
            pm(sender, "&cThat arena is currently in use. To force-end a match, type /practice kill [name].");
            return;
        }

        manager.removeArena(arena);
        pm(sender, Lang.ARENA_DELETE.getMessage().replace("{NAME}", name));
    }
}
