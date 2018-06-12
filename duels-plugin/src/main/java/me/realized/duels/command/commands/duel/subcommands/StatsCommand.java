/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
