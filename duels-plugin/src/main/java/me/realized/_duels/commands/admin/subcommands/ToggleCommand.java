package me.realized._duels.commands.admin.subcommands;

import me.realized._duels.arena.Arena;
import me.realized._duels.commands.SubCommand;
import me.realized._duels.utilities.Helper;
import org.bukkit.entity.Player;

public class ToggleCommand extends SubCommand {

    public ToggleCommand() {
        super("toggle", "toggle [name]", "duels.admin", "Enable/Disable an arena.", 2);
    }

    @Override
    public void execute(Player sender, String[] args) {
        String name = Helper.join(args, 1, args.length, " ");

        if (arenaManager.getArena(name) == null) {
            Helper.pm(sender, "Errors.arena-not-found", true);
            return;
        }

        Arena arena = arenaManager.getArena(name);
        arena.setDisabled(!arena.isDisabled());
        Helper.pm(sender, (arena.isDisabled() ? "Arenas.disabled" : "Arenas.enabled"), true, "{NAME}", name);
    }
}
