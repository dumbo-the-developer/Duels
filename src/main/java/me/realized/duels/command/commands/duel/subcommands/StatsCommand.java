package me.realized.duels.command.commands.duel.subcommands;

import java.util.Optional;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.data.UserData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand extends BaseCommand {

    public StatsCommand(final DuelsPlugin plugin) {
        super(plugin, "stats", null, null, "Displays your duel stats.", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length > getLength()) {
            final Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null) {
                // boi
                return;
            }

            displayStats(sender, target);
        }

        displayStats(sender, (Player) sender);
    }

    private void displayStats(final CommandSender sender, final Player player) {
        final Optional<UserData> cached = userManager.get(player);

        if (!cached.isPresent()) {
            // boi
            return;
        }

        final UserData data = cached.get();
        sender.sendMessage("Stats of " + data.getName() + " -");
        sender.sendMessage("Wins: " + data.getWins());
        sender.sendMessage("Losses: " + data.getLosses());
        sender.sendMessage("W/L Ratio: " + (double) data.getWins() / (double) data.getLosses());
    }
}
