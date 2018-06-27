package me.realized.duels.command.commands.duel.subcommands;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.user.UserManager.TopEntry;
import me.realized.duels.api.util.Pair;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.extra.Permissions;
import me.realized.duels.kit.Kit;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public class TopCommand extends BaseCommand {

    public TopCommand(final DuelsPlugin plugin) {
        super(plugin, "top", "top [wins|losses|kit]", "Displays top wins, losses, or rating for kit.", Permissions.TOP, 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (!userManager.isLoaded()) {
            lang.sendMessage(sender, "ERROR.data.not-loaded");
            return;
        }

        final TopEntry topEntry;

        if (args[1].equalsIgnoreCase("wins")) {
            topEntry = userManager.getWins();
        } else if (args[1].equalsIgnoreCase("losses")) {
            topEntry = userManager.getLosses();
        } else {
            final String name = StringUtils.join(args, " ", 1, args.length);
            final Kit kit = kitManager.get(name);

            if (kit == null) {
                lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
                return;
            }

            topEntry = userManager.getTopRatings().get(kit);
        }

        final List<Pair<String, Integer>> top;

        if (topEntry == null || (top = topEntry.getData()).isEmpty()) {
            lang.sendMessage(sender, "ERROR.top.no-data-available");
            return;
        }

        lang.sendMessage(sender, "COMMAND.duel.top.next-update", "remaining", userManager.getNextUpdate(topEntry.getCreation()));
        lang.sendMessage(sender, "COMMAND.duel.top.header", "type", topEntry.getName());

        for (int i = 0; i < top.size(); i++) {
            final Pair<String, Integer> entry = top.get(i);
            lang.sendMessage(sender, "COMMAND.duel.top.display-format",
                "rank", i + 1, "name", entry.getKey(), "score", entry.getValue(), "identifier", topEntry.getType());
        }

        lang.sendMessage(sender, "COMMAND.duel.top.footer", "type", topEntry.getName());
    }
}
