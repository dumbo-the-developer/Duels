package me.realized.duels.command.commands.duel.subcommands;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.data.MatchData;
import me.realized.duels.data.UserData;
import me.realized.duels.extra.Permissions;
import me.realized.duels.util.DateUtil;
import me.realized.duels.util.TextBuilder;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand extends BaseCommand {

    public StatsCommand(final DuelsPlugin plugin) {
        super(plugin, "stats", Permissions.STATS);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (args.length > getLength()) {
            if (!sender.hasPermission(Permissions.STATS_OTHERS)) {
                lang.sendMessage(sender, "ERROR.no-permission", "permission", Permissions.STATS_OTHERS);
                return;
            }

            final Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null || !player.canSee(target)) {
                lang.sendMessage(sender, "ERROR.player-not-found", "name", args[1]);
                return;
            }

            displayStats(player, target);
            return;
        }

        displayStats(player, player);
    }

    private void displayStats(final Player sender, final Player player) {
        final UserData user = userManager.get(player);

        if (user == null) {
            // boi
            return;
        }

        final Map<String, Integer> rating = user.getRating();

        if (rating != null) {
            rating.forEach((name, value) -> sender.sendMessage(name + ": " + value + " Rating"));
        }

        final String wins = String.valueOf(user.getWins());
        final String losses = String.valueOf(user.getLosses());
        final String wlRatio = String.valueOf(Math.round(((double) user.getWins() / (double) user.getLosses()) * 100.0) / 100.0);
        final String requests = String.valueOf(user.canRequest() ? "&aenabled" : "&cdisabled");
        lang.sendMessage(sender, "COMMAND.duel.stats.displayed",
            "player", user.getName(), "wins", wins, "losses", losses, "wl_ratio", wlRatio, "requests_enabled", requests);

        if (config.isDisplayPastMatches()) {
            final Calendar calendar = new GregorianCalendar();

            for (final MatchData match : user.getMatches()) {
                final String duration = DateUtil.formatMilliseconds(match.getDuration());
                final String timeSince = DateUtil.formatMilliseconds(calendar.getTimeInMillis() - match.getTime());
                TextBuilder
                    .of(lang.getMessage("COMMAND.duel.stats.match-format", "winner", match.getWinner(), "loser", match.getLoser()))
                    .setHoverEvent(Action.SHOW_TEXT,
                        lang.getMessage("COMMAND.duel.stats.hover-text", "duration", duration, "time", timeSince, "health", match.getHealth()))
                    .send(sender);
            }
        }

        lang.sendMessage(sender, "COMMAND.duel.stats.extra");
    }
}
