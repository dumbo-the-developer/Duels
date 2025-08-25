package com.meteordevelopments.duels.lb.manager;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.lb.LeaderboardEntry;
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

    public List<LeaderboardEntry> getTotalEloLeaderboard() {
        long currentTime = System.currentTimeMillis();
        Long lastUpdate = lastUpdateTimes.get("total_elo");
        
        // Check if we need to update the leaderboard
        if (lastUpdate == null || currentTime - lastUpdate > UPDATE_INTERVAL) {
            updateLeaderboard("total_elo");
            lastUpdateTimes.put("total_elo", currentTime);
        }
        
        return leaderboards.getOrDefault("total_elo", new ArrayList<>());
    }

    public LeaderboardEntry getTotalEloEntry(int position) {
        List<LeaderboardEntry> leaderboard = getTotalEloLeaderboard();
        if (position > 0 && position <= leaderboard.size()) {
            return leaderboard.get(position - 1);
        }
        return null;
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
