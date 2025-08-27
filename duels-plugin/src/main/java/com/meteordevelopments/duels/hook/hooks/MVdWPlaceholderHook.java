package com.meteordevelopments.duels.hook.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.lb.LeaderboardEntry;
import com.meteordevelopments.duels.rank.Rank;
import com.meteordevelopments.duels.data.UserData;
import com.meteordevelopments.duels.data.UserManagerImpl;
import com.meteordevelopments.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

public class MVdWPlaceholderHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "MVdWPlaceholderAPI";

    private final UserManagerImpl userDataManager;
    private final DuelsPlugin plugin;

    public MVdWPlaceholderHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.userDataManager = plugin.getUserManager();
        this.plugin = plugin;

        final Placeholders placeholders = new Placeholders();
        PlaceholderAPI.registerPlaceholder(plugin, "duels_wins", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "duels_losses", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "duels_can_request", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "duels_total_elo", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "duels_rank_name", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "duels_rank_desc", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "duels_rank_color", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "duels_rank_progress", placeholders);
        
        // Register leaderboard placeholders
        for (int i = 1; i <= 10; i++) {
            PlaceholderAPI.registerPlaceholder(plugin, "duels_elo_leaderboard_" + i, placeholders);
        }
    }

    public class Placeholders implements PlaceholderReplacer {

        @Override
        public String onPlaceholderReplace(final PlaceholderReplaceEvent event) {
            final Player player = event.getPlayer();

            if (player == null) {
                return "Player is required";
            }

            final UserData user = userDataManager.get(player);

            if (user == null) {
                return null;
            }

            switch (event.getPlaceholder()) {
                case "duels_wins":
                    return String.valueOf(user.getWins());
                case "duels_losses":
                    return String.valueOf(user.getLosses());
                case "duels_can_request":
                    return String.valueOf(user.canRequest());
                case "duels_total_elo":
                    return String.valueOf(user.getTotalElo());
                case "duels_rank_name":
                    if (!plugin.getRankManager().isEnabled()) {
                        return "Rank system disabled";
                    }
                    Rank rank = plugin.getRankManager().getPlayerRank(player);
                    return rank != null ? rank.getColoredName() : "Unknown";
                case "duels_rank_desc":
                    if (!plugin.getRankManager().isEnabled()) {
                        return "Rank system disabled";
                    }
                    rank = plugin.getRankManager().getPlayerRank(player);
                    return rank != null ? rank.getDescription() : "No description";
                case "duels_rank_color":
                    if (!plugin.getRankManager().isEnabled()) {
                        return "&7";
                    }
                    rank = plugin.getRankManager().getPlayerRank(player);
                    return rank != null ? rank.getColor() : "&7";
                case "duels_rank_progress":
                    if (!plugin.getRankManager().isEnabled()) {
                        return "0";
                    }
                    rank = plugin.getRankManager().getPlayerRank(player);
                    if (rank == null) {
                        return "0";
                    }
                    double progress = rank.getProgress(user.getTotalElo());
                    return String.format("%.1f", progress);
            }

            // Handle ELO by kit placeholders: duels_elo_<kitname>
            if (event.getPlaceholder().startsWith("duels_elo_")) {
                String kitName = event.getPlaceholder().replace("duels_elo_", "");
                com.meteordevelopments.duels.api.kit.Kit kit = plugin.getKitManager().get(kitName);
                return kit != null ? String.valueOf(user.getRating(kit)) : "0";
            }

            // Handle ELO leaderboard placeholders: duels_elo_leaderboard_<position>
            if (event.getPlaceholder().startsWith("duels_elo_leaderboard_")) {
                String positionStr = event.getPlaceholder().replace("duels_elo_leaderboard_", "");
                try {
                    int position = Integer.parseInt(positionStr);
                    if (position < 1 || position > 10) {
                        return "Invalid position";
                    }
                    
                    LeaderboardEntry entry =
                        plugin.getLeaderboardManager().getTotalEloEntry(position);
                    return entry != null ? entry.playerName() : "N/A";
                } catch (NumberFormatException e) {
                    return "Invalid position";
                }
            }

            return null;
        }
    }
}
