package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.commands.SubCommand;
import me.realized.duels.utilities.Lang;
import org.bukkit.command.CommandSender;

public class ToggleCommand extends SubCommand {

    private final ArenaManager manager;

    public ToggleCommand() {
        super("toggle", "toggle [name]", "Enable/Disable an arena.", 2);
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
        arena.setDisabled(!arena.isDisabled());
        pm(sender, (arena.isDisabled() ? Lang.ARENA_TOGGLE_OFF : Lang.ARENA_TOGGLE_ON).getMessage().replace("{NAME}", name));
    }
}
