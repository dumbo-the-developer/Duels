package me.realized._duels.commands.duel.subcommands;

import me.realized._duels.commands.SubCommand;
import me.realized._duels.data.MatchData;
import me.realized._duels.data.UserData;
import me.realized._duels.utilities.Helper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

public class StatsCommand extends SubCommand {

    public StatsCommand() {
        super("stats", "stats", "duels.stats", "View stats of players.", 1);
    }

    @Override
    public void execute(Player sender, String[] args) {
        if (args.length <= 1) {
            if (dataManager.getUser(sender.getUniqueId(), sender.hasPermission("duels.admin")) == null) {
                Helper.pm(sender, "&c&lYour data is improperly loaded. Please try re-logging.", false);
                return;
            }

            UserData target = dataManager.getUser(sender.getUniqueId(), false);
            target.refreshMatches();
            displayStats(sender, target);
            return;
        }

        UUID uuid = Helper.getUUID(args[1]);

        if (uuid == null) {
            Helper.pm(sender, "Errors.player-not-found", true);
            return;
        }

        UserData target = dataManager.getUser(uuid, sender.hasPermission("duels.admin"));

        if (target == null) {
            Helper.pm(sender, "Errors.player-not-found", true);
            return;
        }

        target.refreshMatches();
        displayStats(sender, target);
    }
    
    private void displayStats(Player base, UserData user) {
        Calendar calendar = new GregorianCalendar();
        String wins = String.valueOf(user.get(UserData.StatsType.WINS));
        String losses = String.valueOf(user.get(UserData.StatsType.LOSSES));
        String requests = String.valueOf(user.canRequest() ? "enabled" : "disabled");
        Helper.pm(base, "Stats.displayed", true, "{NAME}", user.getName(), "{WINS}", wins, "{LOSSES}", losses, "{REQUESTS_ENABLED}", requests);

        if (config.isStatsDisplayMatches()) {
            for (int i = user.getMatches().size() - 1; i >= 0; i--) {
                MatchData match = user.getMatches().get(i);
                String duration = Helper.toHumanReadableTime(match.getDuration());
                String timeSince = Helper.toHumanReadableTime(calendar.getTimeInMillis() - match.getTime());
                BaseComponent[] text = TextComponent.fromLegacyText(Helper.replaceWithArgs(Helper.color(messages.getString("Stats.match-format")), "{WINNER}", match.getWinner(), "{LOSER}", match.getLoser()));
                BaseComponent[] hover = TextComponent.fromLegacyText(Helper.replaceWithArgs(Helper.color(Helper.join(messages.getList("Stats.match-hover"), "\n")), "{DURATION}", duration, "{TIME}", timeSince, "{HEALTH}", match.getHealth()));

                for (BaseComponent line : text) {
                    line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                }

                base.spigot().sendMessage(text);
            }
        }
    }
}
