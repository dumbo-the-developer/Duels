package me.realized.duels.command.commands.duels;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duels.subcommands.CreateCommand;
import me.realized.duels.command.commands.duels.subcommands.DeletekitCommand;
import me.realized.duels.command.commands.duels.subcommands.LoadkitCommand;
import me.realized.duels.command.commands.duels.subcommands.SavekitCommand;
import me.realized.duels.command.commands.duels.subcommands.SetCommand;
import me.realized.duels.command.commands.duels.subcommands.ToggleCommand;
import me.realized.duels.gui.betting.BettingGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelsCommand extends BaseCommand {

    public DuelsCommand(final DuelsPlugin plugin) {
        super(plugin, "duels", "duels.admin", true);
        child(
            new LoadkitCommand(plugin),
            new SavekitCommand(plugin),
            new DeletekitCommand(plugin),
            new CreateCommand(plugin),
            new DeletekitCommand(plugin),
            new SetCommand(plugin),
            new ToggleCommand(plugin)
        );
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final BettingGui bettingGui;
        guiListener.addGui(bettingGui = new BettingGui((Player) sender, (Player) sender));
        bettingGui.open((Player) sender);
    }
}
