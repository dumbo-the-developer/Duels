package com.meteordevelopments.duels.hook.hooks;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.arena.Arena;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.api.match.Match;
import com.meteordevelopments.duels.api.spectate.Spectator;
import com.meteordevelopments.duels.api.user.User;
import com.meteordevelopments.duels.lb.LeaderboardEntry;
import com.meteordevelopments.duels.rank.Rank;
import com.meteordevelopments.duels.util.StringUtil;
import com.meteordevelopments.duels.util.compat.Ping;
import com.meteordevelopments.duels.util.hook.PluginHook;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "PlaceholderAPI";

    public PlaceholderHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        new Placeholders().register();
    }

    public class Placeholders extends PlaceholderExpansion {
        @Override
        public String getIdentifier() {
            return "duels";
        }

        @Override
        public String getAuthor() {
            return "DUMBO";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
            if (player == null) {
                return "Player is required";
            }

            User user;
            switch (identifier) {
                case "wins":
                    user = plugin.getUserManager().get(player);

                    if (user == null) {
                        return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                    }
                    return String.valueOf(user.getWins());
                case "losses":
                    user = plugin.getUserManager().get(player);
                    if (user == null) {
                        return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                    }
                    return String.valueOf(user.getLosses());
                case "can_request":
                    user = plugin.getUserManager().get(player);
                    if (user == null) {
                        return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                    }
                    return String.valueOf(user.canRequest());
                //case "hits": {
                //    Arena arena = plugin.getArenaManager().get(player);
                //    // Only activate when winner is undeclared
                //    if (arena == null) {
                //        return "-1";
                //    }
                //    return String.valueOf(arena.getMatch().getHits(player));
                //}
                //case "hits_opponent": {
                //    Arena arena = plugin.getArenaManager().get(player);
                //    // Only activate when winner is undeclared
                //    if (arena == null) {
                //        return "-1";
                //    }
                //    return String.valueOf(arena.getMatch().getHits(arena.getOpponent(player)));
                //}
                case "wl_ratio":
                case "wlr":
                    user = plugin.getUserManager().get(player);
                    if (user == null) {
                        return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                    }
                    int wins = user.getWins();
                    int losses = user.getLosses();
                    return String.valueOf(wlr(wins, losses));
            }

            if (identifier.startsWith("rating_")) {
                user = plugin.getUserManager().get(player);

                if (user == null) {
                    return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                }

                identifier = identifier.replace("rating_", "");

                if (identifier.equals("-")) {
                    return String.valueOf(user.getRating());
                }

                final Kit kit = plugin.getKitManager().get(identifier);
                return kit != null ? String.valueOf(user.getRating(kit)) : StringUtil.color(plugin.getConfiguration().getNoKit());
            }

            // Total ELO placeholder
            if (identifier.equals("total_elo")) {
                user = plugin.getUserManager().get(player);

                if (user == null) {
                    return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                }

                return String.valueOf(user.getTotalElo());
            }

            // ELO by kit placeholders: %duels_elo_<kitname>%
            if (identifier.startsWith("elo_")) {
                user = plugin.getUserManager().get(player);

                if (user == null) {
                    return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                }

                identifier = identifier.replace("elo_", "");

                final Kit kit = plugin.getKitManager().get(identifier);
                return kit != null ? String.valueOf(user.getRating(kit)) : StringUtil.color(plugin.getConfiguration().getNoKit());
            }

            // ELO leaderboard placeholders: %duels_elo_leaderboard_<position>%
            if (identifier.startsWith("elo_leaderboard_")) {
                String positionStr = identifier.replace("elo_leaderboard_", "");
                try {
                    int position = Integer.parseInt(positionStr);
                    if (position < 1 || position > 10) {
                        return "Invalid position";
                    }
                    
                    LeaderboardEntry entry = plugin.getLeaderboardManager().getTotalEloEntry(position);
                    return entry != null ? entry.playerName() : "N/A";
                } catch (NumberFormatException e) {
                    return "Invalid position";
                }
            }

            // Rank placeholders
            switch (identifier) {
                case "rank_name" -> {
                    if (!plugin.getRankManager().isEnabled()) {
                        return "Rank system disabled";
                    }

                    Rank rank = plugin.getRankManager().getPlayerRank(player);
                    return rank != null ? rank.getColoredName() : "Unknown";
                }
                case "rank_desc" -> {
                    if (!plugin.getRankManager().isEnabled()) {
                        return "Rank system disabled";
                    }

                    Rank rank = plugin.getRankManager().getPlayerRank(player);
                    return rank != null ? rank.getDescription() : "No description";
                }
                case "rank_color" -> {
                    if (!plugin.getRankManager().isEnabled()) {
                        return "&7";
                    }

                    Rank rank = plugin.getRankManager().getPlayerRank(player);
                    return rank != null ? rank.getColor() : "&7";
                }
                case "rank_progress" -> {
                    if (!plugin.getRankManager().isEnabled()) {
                        return "0";
                    }

                    Rank rank = plugin.getRankManager().getPlayerRank(player);
                    if (rank == null) {
                        return "0";
                    }

                    user = plugin.getUserManager().get(player);
                    if (user == null) {
                        return "0";
                    }

                    double progress = rank.getProgress(user.getTotalElo());
                    return String.format("%.1f", progress);
                }
            }

            if (identifier.startsWith("getplayersinqueue_")){
                user = plugin.getUserManager().get(player);
                if (user == null) {
                    return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                }

                identifier = identifier.replace("getplayersinqueue_", "");

                final Kit kit = plugin.getKitManager().get(identifier);
                if (kit == null) {
                    return StringUtil.color(plugin.getConfiguration().getNoKit());
                }

                int queuedPlayers = plugin.getQueueManager().get(kit, 0).getQueuedPlayers().size();
                return queuedPlayers > 0 ? String.valueOf(queuedPlayers) : "0";
            }

            if (identifier.startsWith("getplayersplayinginqueue_")){
                user = plugin.getUserManager().get(player);
                if (user == null) {
                    return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                }
                identifier = identifier.replace("getplayersplayinginqueue_", "");
                final Kit kit = plugin.getKitManager().get(identifier);
                if (kit == null) {
                    return StringUtil.color(plugin.getConfiguration().getNoKit());
                }
                long playersInMatch = plugin.getQueueManager().get(kit, 0).getPlayersInMatch();
                return Long.toString(playersInMatch);
            }

            if (identifier.startsWith("match_")) {
                identifier = identifier.replace("match_", "");
                Arena arena = plugin.getArenaManager().get(player);

                if (arena == null) {
                    final Spectator spectator = plugin.getSpectateManager().get(player);

                    if (spectator == null) {
                        return StringUtil.color(plugin.getConfiguration().getNotInMatch());
                    }

                    arena = spectator.getArena();
                    player = spectator.getTarget();

                    if (player == null) {
                        return StringUtil.color(plugin.getConfiguration().getNotInMatch());
                    }
                }

                final Match match = arena.getMatch();

                if (match == null) {
                    return StringUtil.color(plugin.getConfiguration().getNotInMatch());
                }

                if (identifier.equalsIgnoreCase("duration")) {
                    return DurationFormatUtils.formatDuration(System.currentTimeMillis() - match.getStart(), plugin.getConfiguration().getDurationFormat());
                }

                if (identifier.equalsIgnoreCase("kit")) {
                    return match.getKit() != null ? match.getKit().getName() : StringUtil.color(plugin.getConfiguration().getNoKit());
                }

                if (identifier.equalsIgnoreCase("arena")) {
                    return match.getArena().getName();
                }

                if (identifier.equalsIgnoreCase("bet")) {
                    return String.valueOf(match.getBet());
                }

                if (identifier.equalsIgnoreCase("rating")) {
                    user = plugin.getUserManager().get(player);

                    if (user == null) {
                        return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                    }

                    return String.valueOf(match.getKit() != null ? user.getRating(match.getKit()) : user.getRating());
                }

                if (identifier.startsWith("opponent")) {
                    Player opponent = null;

                    for (final Player matchPlayer : match.getPlayers()) {
                        if (!matchPlayer.equals(player)) {
                            opponent = matchPlayer;
                            break;
                        }
                    }

                    if (opponent == null) {
                        return StringUtil.color(plugin.getConfiguration().getNoOpponent());
                    }

                    if (identifier.equalsIgnoreCase("opponent")) {
                        return opponent.getName();
                    }

                    if (identifier.endsWith("_health")) {
                        return String.valueOf(Math.ceil(opponent.getHealth()) * 0.5);
                    }

                    if (identifier.endsWith("_ping")) {
                        return String.valueOf(Ping.getPing(opponent));
                    }

                    user = plugin.getUserManager().get(opponent);

                    if (user == null) {
                        return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                    }

                    return String.valueOf(match.getKit() != null ? user.getRating(match.getKit()) : user.getRating());
                }
            }
            return null;
        }

        private float wlr(int wins, int losses) {
            if (wins == 0) {
                return losses == 0 ? 0.0F : (float)(-losses);
            } else if (losses == 0) {
                return (float)wins;
            } else {
                return (float)(wins / losses);
            }
        }
    }
}