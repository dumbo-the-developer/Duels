package me.realized.duels.command.commands.duel.subcommands;

import java.util.Calendar;
import java.util.GregorianCalendar;
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
        super(plugin, "stats", null, null, Permissions.STATS, 1, true);
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
                lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
                return;
            }

            displayStats(player, target);
            return;
        }

        displayStats(player, player);
    }

    private void displayStats(final Player sender, final Player target) {
        final UserData user = userManager.get(target);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "player", target.getName());
            return;
        }

        final String wins = String.valueOf(user.getWins());
        final String losses = String.valueOf(user.getLosses());
        final String wlRatio = String.valueOf(Math.round(((double) user.getWins() / (double) user.getLosses()) * 100.0) / 100.0);
        final String requests = String.valueOf(user.canRequest() ? "&aenabled" : "&cdisabled");
        final Object[] args = {"player", user.getName(), "wins", wins, "losses", losses, "wl_ratio", wlRatio, "requests_enabled", requests};
        lang.sendMessage(sender, "COMMAND.duel.stats.displayed", args);

        if (config.isDisplayRatings()) {
            lang.sendMessage(sender, "COMMAND.duel.stats.rating.header", args);
            kitManager.getKits().forEach(kit -> lang.sendMessage(sender, "COMMAND.duel.stats.rating.format", "kit", kit.getName(), "rating", user.getRating(kit)));
            lang.sendMessage(sender, "COMMAND.duel.stats.rating.footer", args);
        }

        if (config.isDisplayPastMatches()) {
            lang.sendMessage(sender, "COMMAND.duel.stats.match.header", args);

            final Calendar calendar = new GregorianCalendar();

            for (final MatchData match : user.getMatches()) {
                final String duration = DateUtil.formatMilliseconds(match.getDuration());
                final String timeSince = DateUtil.formatMilliseconds(calendar.getTimeInMillis() - match.getTime());
                TextBuilder
                    .of(lang.getMessage("COMMAND.duel.stats.match.format", "winner", match.getWinner(), "loser", match.getLoser()))
                    .setHoverEvent(Action.SHOW_TEXT,
                        lang.getMessage("COMMAND.duel.stats.match.hover-text", "duration", duration, "time", timeSince, "health", match.getHealth()))
                    .send(sender);
            }

            lang.sendMessage(sender, "COMMAND.duel.stats.match.footer", args);
        }
    }
}
