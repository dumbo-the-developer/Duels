package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.utilities.Helper;
import org.bukkit.entity.Player;

public class CreateCommand extends SubCommand {

    public CreateCommand() {
        super("create", "create [name]", "duels.admin", "Create an arena.", 2);
    }

    @Override
    public void execute(Player sender, String[] args) {
        String name = Helper.join(args, 1, args.length, " ");

        if (!Helper.isAlphanumeric(name)) {
            Helper.pm(sender, "Errors.must-be-alphanumeric", true);
            return;
        }

        if (arenaManager.getArena(name) != null) {
            Helper.pm(sender, "Errors.arena-exists", true);
            return;
        }

        arenaManager.createArena(name);
        Helper.pm(sender, "Arenas.created", true, "{NAME}", name);
    }
}
