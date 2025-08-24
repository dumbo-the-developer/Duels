package com.meteordevelopments.duels.data;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.util.Loadable;
import lombok.Getter;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LeaderboardManager implements Loadable {

    private final DuelsPlugin plugin;
    private final Map<String, List<LeaderboardEntry>> leaderboards = new ConcurrentHashMap<>();
    private final Map<String, Long> lastUpdateTimes = new ConcurrentHashMap<>();
    private ScheduledTask updateTask;
    
    @Getter
    private static final long UPDATE_INTERVAL = 300000; // 5 minutes in milliseconds
    @Getter
    private static final int MAX_ENTRIES = 10;

    public LeaderboardManager(DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleLoad() {
        startUpdateTask();
    }

    @Override
    public void handleUnload() {
        shutdown();
    }

    /**
     * Gets the leaderboard for total ELO
     * @return List of top players by total ELO
     */
    public List<LeaderboardEntry> getTotalEloLeaderboard() {
        return getLeaderboard("total_elo");
    }

    /**
     * Gets the leaderboard for a specific kit
     * @param kit The kit to get leaderboard for
     * @return List of top players by kit ELO
     */
    public List<LeaderboardEntry> getKitLeaderboard(Kit kit) {
        return getLeaderboard("kit_" + kit.getName());
    }

    /**
     * Gets a leaderboard entry by position for total ELO
     * @param position The position (1-10)
     * @return LeaderboardEntry or null if position doesn't exist
     */
    public LeaderboardEntry getTotalEloEntry(int position) {
        List<LeaderboardEntry> leaderboard = getTotalEloLeaderboard();
        if (position > 0 && position <= leaderboard.size()) {
            return leaderboard.get(position - 1);
        }
        return null;
    }

    /**
     * Gets a leaderboard entry by position for a specific kit
     * @param kit The kit
     * @param position The position (1-10)
     * @return LeaderboardEntry or null if position doesn't exist
     */
    public LeaderboardEntry getKitEntry(Kit kit, int position) {
        List<LeaderboardEntry> leaderboard = getKitLeaderboard(kit);
        if (position > 0 && position <= leaderboard.size()) {
            return leaderboard.get(position - 1);
        }
        return null;
    }

    private List<LeaderboardEntry> getLeaderboard(String key) {
        long currentTime = System.currentTimeMillis();
        Long lastUpdate = lastUpdateTimes.get(key);
        
        // Check if we need to update the leaderboard
        if (lastUpdate == null || currentTime - lastUpdate > UPDATE_INTERVAL) {
            updateLeaderboard(key);
            lastUpdateTimes.put(key, currentTime);
        }
        
        return leaderboards.getOrDefault(key, new ArrayList<>());
    }

    private void updateLeaderboard(String key) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        
        if ("total_elo".equals(key)) {
            // Calculate total ELO leaderboard
            entries = plugin.getUserManager().getAllUsers().stream()
                    .map(user -> new LeaderboardEntry(user.getName(), user.getTotalElo()))
                    .sorted(Comparator.comparingInt(LeaderboardEntry::elo).reversed())
                    .limit(MAX_ENTRIES)
                    .collect(Collectors.toList());
        } else if (key.startsWith("kit_")) {
            // Calculate kit-specific leaderboard
            String kitName = key.substring(4); // Remove "kit_" prefix
            Kit kit = plugin.getKitManager().get(kitName);
            
            if (kit != null) {
                entries = plugin.getUserManager().getAllUsers().stream()
                        .map(user -> new LeaderboardEntry(user.getName(), user.getRating(kit)))
                        .sorted(Comparator.comparingInt(LeaderboardEntry::elo).reversed())
                        .limit(MAX_ENTRIES)
                        .collect(Collectors.toList());
            }
        }
        
        leaderboards.put(key, entries);
    }

    private void startUpdateTask() {
        updateTask = plugin.doSyncRepeat(() -> {
            // Force update all leaderboards
            updateLeaderboard("total_elo");
            
            // Update kit leaderboards
            for (Kit kit : plugin.getKitManager().getKits()) {
                updateLeaderboard("kit_" + kit.getName());
            }
        }, 20L * 60 * 5, 20L * 60 * 5); // Update every 5 minutes
    }

    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        leaderboards.clear();
        lastUpdateTimes.clear();
    }

}
