package me.realized.duels.command.commands.duel;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duel.subcommands.StatsCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand extends BaseCommand {

    public DuelCommand(final DuelsPlugin plugin) {
        super(plugin, "duel", "duels.duel", true);
        child(new StatsCommand(plugin));
    }

    @Override
    protected boolean executeFirst(final CommandSender sender, final String label, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/duel [name]");
            return true;
        }

        if (isChild(args[0])) {
            return false;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            sender.sendMessage("Player not found");
            return true;
        }

        sender.sendMessage("duel request sent to " + target.getName());
        target.sendMessage("duel request from " + sender.getName());
        return true;
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {}

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return null;
    }
}
