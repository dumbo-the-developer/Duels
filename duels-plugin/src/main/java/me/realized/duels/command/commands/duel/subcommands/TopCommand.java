package me.realized.duels.command.commands.duel.subcommands;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.user.UserManager.SortedEntry;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.extra.Permissions;
import org.bukkit.command.CommandSender;

public class TopCommand extends BaseCommand {

    public TopCommand(final DuelsPlugin plugin) {
        super(plugin, "top", "top [wins|losses]", "Displays top duel wins & losses.", Permissions.TOP, 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final boolean wins = args[1].equalsIgnoreCase("wins");
        final List<SortedEntry<String, Integer>> top = wins ? userManager.getTopWins() : userManager.getTopLosses();

        if (top == null || top.isEmpty()) {
            lang.sendMessage(sender, "ERROR.no-data-available");
            return;
        }

        lang.sendMessage(sender, "COMMAND.duel.top.next-update",
            "remaining", wins ? userManager.getNextWinsUpdate() : userManager.getNextLossesUpdate());
        lang.sendMessage(sender, "COMMAND.duel.top.header", "type", wins ? "Wins" : "Losses");

        for (int i = 0; i < top.size(); i++) {
            final SortedEntry<String, Integer> entry = top.get(i);
            lang.sendMessage(sender, "COMMAND.duel.top.display-format",
                "rank", i + 1, "name", entry.getKey(), "score", entry.getValue(), "type", wins ? "Wins" : "Losses");
        }

        lang.sendMessage(sender, "COMMAND.duel.top.footer", "type", wins ? "Wins" : "Losses");
    }
}
