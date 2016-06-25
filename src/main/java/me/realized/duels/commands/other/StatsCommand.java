package me.realized.duels.commands.other;

import me.realized.duels.Core;
import me.realized.duels.commands.BaseCommand;
import me.realized.duels.configuration.Config;
import me.realized.duels.data.DataManager;
import me.realized.duels.data.MatchData;
import me.realized.duels.data.UserData;
import me.realized.duels.utilities.Helper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class StatsCommand extends BaseCommand {

    private final Core instance;
    private final Config config;
    private final DataManager manager;

    public StatsCommand() {
        super("stats", "duels.stats");
        this.instance = getInstance();
        this.config = instance.getConfiguration();
        this.manager = instance.getDataManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            if (manager.getUser(player.getUniqueId(), player.hasPermission("duels.admin")) == null) {
                pm(sender, "&c&lYour data is improperly loaded. Please try re-logging.");
                return;
            }

            displayStats(player, manager.getUser(player.getUniqueId(), false));
            return;
        }

        UUID uuid = Helper.getUUID(args[0]);

        if (uuid == null) {
            pm(player, "&cThat player was not found.");
            return;
        }

        UserData target = manager.getUser(uuid, player.hasPermission("duels.admin"));

        if (target == null) {
            pm(player, "&cThat player was not found.");
            return;
        }

        displayStats(player, target);
    }
    
    private void displayStats(Player base, UserData user) {
        Calendar calendar = new GregorianCalendar();
        String wins = String.valueOf(user.get(UserData.StatsType.WINS));
        String losses = String.valueOf(user.get(UserData.StatsType.LOSSES));
        String requests = String.valueOf(user.canRequest() ? "enabled" : "disabled");
        List<String> stats = config.getList("stats");

        for (String txt : stats) {
            txt = Helper.replaceWithArgs(Helper.color(txt), "{NAME}", user.getName(), "{WINS}", wins, "{LOSSES}", losses, "{REQUESTS_ENABLED}", requests);
            pm(base, txt);
        }

        if (config.getBoolean("display-matches")) {
            for (MatchData match : user.getMatches()) {
                String duration = Helper.toHumanReadableTime(match.getDuration());
                String timeSince = Helper.toHumanReadableTime(calendar.getTimeInMillis() - match.getTime());
                BaseComponent[] messages = TextComponent.fromLegacyText(Helper.replaceWithArgs(Helper.color(config.getString("match-format")), "{WINNER}", match.getWinner(), "{LOSER}", match.getLoser()));
                BaseComponent[] hover = TextComponent.fromLegacyText(Helper.replaceWithArgs(Helper.color(config.getString("match-hover")), "{DURATION}", duration, "{TIME}", timeSince, "{HEALTH}", match.getHealth()));

                for (BaseComponent message : messages) {
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                }

                base.spigot().sendMessage(messages);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
}
