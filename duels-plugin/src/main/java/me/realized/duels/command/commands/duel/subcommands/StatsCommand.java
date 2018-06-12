package me.realized.duels.command.commands.duel.subcommands;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.data.MatchData;
import me.realized.duels.data.UserData;
import me.realized.duels.util.DateUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand extends BaseCommand {

    public StatsCommand(final DuelsPlugin plugin) {
        super(plugin, "stats", "duels.stats");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (args.length > getLength()) {
            if (!sender.hasPermission(getPermission() + ".others")) {
                lang.sendMessage(sender, "ERROR.no-permission", "permission", getPermission() + ".others");
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

        final Calendar calendar = new GregorianCalendar();

        for (int i = user.getMatches().size() - 1; i >= 0; i--) {
            final MatchData match = user.getMatches().get(i);
            final String duration = DateUtil.formatMilliseconds(match.getDuration());
            final String timeSince = DateUtil.formatMilliseconds(calendar.getTimeInMillis() - match.getTime());
            String message = lang.getMessage("COMMAND.duel.stats.match-format", "winner", match.getWinner(), "loser", match.getLoser());

            if (message == null) {
                break;
            }

            final BaseComponent[] text = TextComponent.fromLegacyText(message);
            message = lang.getMessage("COMMAND.duel.stats.hover-text", "duration", duration, "time", timeSince, "health", match.getHealth());

            if (message == null) {
                break;
            }

            final BaseComponent[] hoverText = TextComponent.fromLegacyText(message);

            for (final BaseComponent component : text) {
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
            }

            sender.spigot().sendMessage(text);
        }
    }
}
