package com.meteordevelopments.duels.hook.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.data.UserData;
import com.meteordevelopments.duels.data.UserManagerImpl;
import com.meteordevelopments.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

public class PlaceholderHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "PlaceholderAPI";

    private final UserManagerImpl userDataManager;

    public PlaceholderHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.userDataManager = plugin.getUserManager();
        new Placeholders().register();
    }

    public class Placeholders extends PlaceholderExpansion {

        @Override
        public String getIdentifier() {
            return "duels";
        }

        @Override
        public String getAuthor() {
            return "Realized";
        }

        @Override
        public String getVersion() {
            return plugin.getVersion();
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onPlaceholderRequest(final Player player, final String identifier) {
            if (player == null) {
                return "Player is required";
            }

            final UserData user = userDataManager.get(player);

            if (user == null) {
                return null;
            }

            switch (identifier) {
                case "wins":
                    return String.valueOf(user.getWins());
                case "losses":
                    return String.valueOf(user.getLosses());
                case "can_request":
                    return String.valueOf(user.canRequest());
                case "hits": {
                    final ArenaManagerImpl arenaManager = plugin.getArenaManager();
                    final ArenaImpl arena = arenaManager.get(player);

                    // Only activate when winner is undeclared
                    if (arena == null || !arenaManager.isInMatch(player) || arena.isEndGame()) {
                        return "-1";
                    }
                    return String.valueOf(arena.getMatch().getHits(player));
                }
                case "hits_opponent": {
                    final ArenaManagerImpl arenaManager = plugin.getArenaManager();
                    final ArenaImpl arena = arenaManager.get(player);

                    // Only activate when winner is undeclared
                    if (arena == null || !arenaManager.isInMatch(player) || arena.isEndGame()) {
                        return "-1";
                    }
                    return String.valueOf(arena.getMatch().getHits(arena.getOpponent(player)));
                }
            }

            return null;
        }
    }
}
