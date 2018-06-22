package me.realized.duels.command.commands.duels;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duels.subcommands.AddsignCommand;
import me.realized.duels.command.commands.duels.subcommands.CreateCommand;
import me.realized.duels.command.commands.duels.subcommands.DeleteCommand;
import me.realized.duels.command.commands.duels.subcommands.DeletekitCommand;
import me.realized.duels.command.commands.duels.subcommands.LoadkitCommand;
import me.realized.duels.command.commands.duels.subcommands.ReloadCommand;
import me.realized.duels.command.commands.duels.subcommands.SavekitCommand;
import me.realized.duels.command.commands.duels.subcommands.SetCommand;
import me.realized.duels.command.commands.duels.subcommands.ToggleCommand;
import me.realized.duels.extra.Permissions;
import org.bukkit.command.CommandSender;

public class DuelsCommand extends BaseCommand {

    public DuelsCommand(final DuelsPlugin plugin) {
        super(plugin, "duels", Permissions.ADMIN, false);
        child(
            new SavekitCommand(plugin),
            new DeletekitCommand(plugin),
            new LoadkitCommand(plugin),
            new CreateCommand(plugin),
            new DeleteCommand(plugin),
            new SetCommand(plugin),
            new ToggleCommand(plugin),
            new AddsignCommand(plugin),
            new ReloadCommand(plugin)
        );
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        lang.sendMessage(sender, "COMMAND.duels.usage");
    }
}
