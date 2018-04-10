package me.realized.duels.command.commands.duel;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duel.subcommands.StatsCommand;
import org.bukkit.command.CommandSender;

public class DuelCommand extends BaseCommand {

    public DuelCommand(final DuelsPlugin plugin) {
        super(plugin, "duel", "duels.duel", true);
        child(new StatsCommand(plugin));
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        sender.sendMessage("boi u dont");
    }
}
