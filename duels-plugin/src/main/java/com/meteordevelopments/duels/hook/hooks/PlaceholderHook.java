package com.meteordevelopments.duels.hook.hooks;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.arena.Arena;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.api.match.Match;
import com.meteordevelopments.duels.api.spectate.Spectator;
import com.meteordevelopments.duels.api.user.User;
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

			if (identifier.startsWith("getplayersinqueue_")){
                user = plugin.getUserManager().get(player);
                if (user == null) {
                    return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                }

				identifier = identifier.replace("getplayersinqueue_", "");

				// Try to find queue by name first
				com.meteordevelopments.duels.api.queue.DQueue queue = plugin.getQueueManager().getByName(identifier);
				
				if (queue == null) {
					// Fallback to old kit-based system
					int bet = 0;
					String kitName = identifier;
					int sep = identifier.lastIndexOf('_');
					if (sep >= 0 && sep + 1 < identifier.length()) {
						String betStr = identifier.substring(sep + 1);
						try {
							bet = Integer.parseInt(betStr);
							kitName = identifier.substring(0, sep);
						} catch (NumberFormatException ignored) {
							// Keep default bet = 0 and full identifier as kit name
						}
					}

					final Kit kit = plugin.getKitManager().get(kitName);
					if (kit == null) {
						return StringUtil.color(plugin.getConfiguration().getNoKit());
					}
					
					queue = plugin.getQueueManager().get(kit, bet);
					if (queue == null) {
						return "0";
					}
				}

				int queuedPlayers = queue.getQueuedPlayers().size();
                return queuedPlayers > 0 ? String.valueOf(queuedPlayers) : "0";
            }

			if (identifier.startsWith("getplayersplayinginqueue_")){
                user = plugin.getUserManager().get(player);
                if (user == null) {
                    return StringUtil.color(plugin.getConfiguration().getUserNotFound());
                }
				identifier = identifier.replace("getplayersplayinginqueue_", "");
				
				// Try to find queue by name first
				com.meteordevelopments.duels.api.queue.DQueue queue = plugin.getQueueManager().getByName(identifier);
				
				if (queue == null) {
					// Fallback to old kit-based system
					int bet = 0;
					String kitName = identifier;
					int sep = identifier.lastIndexOf('_');
					if (sep >= 0 && sep + 1 < identifier.length()) {
						String betStr = identifier.substring(sep + 1);
						try {
							bet = Integer.parseInt(betStr);
							kitName = identifier.substring(0, sep);
						} catch (NumberFormatException ignored) {
							// Keep default bet = 0 and full identifier as kit name
						}
					}
					
					final Kit kit = plugin.getKitManager().get(kitName);
					if (kit == null) {
						return StringUtil.color(plugin.getConfiguration().getNoKit());
					}
					
					queue = plugin.getQueueManager().get(kit, bet);
					if (queue == null) {
						return "0";
					}
				}
				
				long playersInMatch = queue.getPlayersInMatch();
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