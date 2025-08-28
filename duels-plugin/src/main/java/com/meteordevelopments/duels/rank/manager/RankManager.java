package com.meteordevelopments.duels.rank.manager;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.data.UserData;
import com.meteordevelopments.duels.rank.Rank;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Log;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RankManager implements Loadable {
    
    private final DuelsPlugin plugin;
    private final Map<String, Rank> ranks = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerRanks = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> oneTimeRewardsClaimed = new ConcurrentHashMap<>();
    
    @Getter
    private boolean enabled = false;
    
    @Getter
    private String defaultRank = "rutbesiz";
    
    @Getter
    private boolean promotionEnabled = true;
    
    @Getter
    private int commandDelay = 20;
    
    @Getter
    private boolean oneTimeRewardsEnabled = true;
    
    public RankManager(DuelsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void handleLoad() throws Exception {
        loadRanks();
    }
    
    @Override
    public void handleUnload() throws Exception {
        // Clear all cached data
        ranks.clear();
        playerRanks.clear();
        oneTimeRewardsClaimed.clear();
    }
    
    public void loadRanks() {
        File ranksFile = new File(plugin.getDataFolder(), "ranks.yml");
        
        if (!ranksFile.exists()) {
            plugin.saveResource("ranks.yml", false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(ranksFile);
        
        this.enabled = config.getBoolean("ranks.enabled", true);
        this.defaultRank = config.getString("ranks.default-rank", "rutbesiz");
        this.promotionEnabled = config.getBoolean("promotion.enabled", true);
        this.commandDelay = config.getInt("promotion.command-delay", 20);
        this.oneTimeRewardsEnabled = config.getBoolean("one-time-rewards.enabled", true);
        
        if (!enabled) {
            Log.info("Rank system is disabled in configuration");
            return;
        }
        
        // Clear existing ranks
        ranks.clear();
        
        // Load rank definitions
        ConfigurationSection definitions = config.getConfigurationSection("ranks.definitions");
        if (definitions != null) {
            for (String rankId : definitions.getKeys(false)) {
                ConfigurationSection rankSection = definitions.getConfigurationSection(rankId);
                if (rankSection != null) {
                    Rank rank = new Rank(rankId);
                    rank.loadFromConfig(rankSection);
                    ranks.put(rankId, rank);
                }
            }
        }
        
        Log.info("Loaded " + ranks.size() + " ranks from configuration");
        
        // Validate configuration after loading
        validateConfig();
    }
    
    private void validateConfig() {
        // Check if default rank exists
        if (!ranks.containsKey(defaultRank)) {
            Log.warn("Default rank '" + defaultRank + "' is not found in the ranks configuration!");
        }
        
        // Create sorted list of ranks by minElo
        List<Rank> sortedRanks = new ArrayList<>(ranks.values());
        sortedRanks.sort(Comparator.comparingInt(Rank::getMinElo));
        
        // Check for invalid rank ranges and overlaps
        for (int i = 0; i < sortedRanks.size(); i++) {
            Rank current = sortedRanks.get(i);
            
            // Check if minElo > maxElo
            if (current.getMinElo() > current.getMaxElo()) {
                Log.warn("Rank '" + current.getId() + "' has invalid range: minElo (" + current.getMinElo() + ") > maxElo (" + current.getMaxElo() + ")");
            }
            
            // Check for overlaps with previous rank
            if (i > 0) {
                Rank previous = sortedRanks.get(i - 1);
                if (current.getMinElo() <= previous.getMaxElo()) {
                    Log.warn("Rank '" + current.getId() + "' overlaps with '" + previous.getId() + "': " + 
                            current.getMinElo() + " <= " + previous.getMaxElo());
                }
            }
        }
    }
    
    public Rank calculateRank(int elo) {
        // Create a sorted list of ranks by minElo ascending
        List<Rank> sortedRanks = new ArrayList<>(ranks.values());
        sortedRanks.sort(Comparator.comparingInt(Rank::getMinElo));
        
        // If no ranks are configured, return null
        if (sortedRanks.isEmpty()) {
            return null;
        }
        
        // Find the first rank where elo is in range
        for (Rank rank : sortedRanks) {
            if (rank.isInRange(elo)) {
                return rank;
            }
        }
        
        // No rank matches the elo range
        Rank firstRank = sortedRanks.getFirst();
        Rank lastRank = sortedRanks.getLast();
        
        // If elo is below the first range, return the first rank (or defaultRank if configured and present)
        if (elo < firstRank.getMinElo()) {
            Rank defaultRankObj = ranks.get(defaultRank);
            return defaultRankObj != null ? defaultRankObj : firstRank;
        }
        
        // If elo is above the last range, return the last rank
        if (elo > lastRank.getMaxElo()) {
            return lastRank;
        }
        
        // Fallback: return the first rank (lowest-ranked entry) to avoid NPEs
        return firstRank;
    }
    
    public Rank getPlayerRank(Player player) {
        return getPlayerRank(player.getUniqueId());
    }
    
    public Rank getPlayerRank(UUID uuid) {
        UserData userData = plugin.getUserManager().get(uuid);
        if (userData == null) {
            return ranks.get(defaultRank);
        }
        
        return calculateRank(userData.getTotalElo());
    }
    
    public Rank getNextRank(Player player) {
        return getNextRank(player.getUniqueId());
    }
    
    public Rank getNextRank(UUID uuid) {
        UserData userData = plugin.getUserManager().get(uuid);
        if (userData == null) {
            return null;
        }
        
        int currentElo = userData.getTotalElo();
        
        // Find the next rank with higher ELO requirement
        Rank nextRank = null;
        int minEloForNext = Integer.MAX_VALUE;
        
        for (Rank rank : ranks.values()) {
            if (rank.getMinElo() > currentElo && rank.getMinElo() < minEloForNext) {
                nextRank = rank;
                minEloForNext = rank.getMinElo();
            }
        }
        
        return nextRank;
    }
    
    public boolean checkPromotion(UUID uuid) {
        return checkRankChange(uuid, true);
    }
    
    public boolean checkDemotion(UUID uuid) {
        return checkRankChange(uuid, false);
    }
    
    private boolean checkRankChange(UUID uuid, boolean isPromotion) {
        if (!enabled || !promotionEnabled) {
            return false;
        }
        
        UserData userData = plugin.getUserManager().get(uuid);
        if (userData == null) {
            return false;
        }
        
        String previousRankId = playerRanks.get(uuid);
        Rank currentRank = calculateRank(userData.getTotalElo());
        
        // If no previous rank or rank changed
        if (previousRankId == null || !previousRankId.equals(currentRank.getId())) {
            // Update stored rank
            playerRanks.put(uuid, currentRank.getId());
            
            if (previousRankId != null) {
                Rank previousRank = ranks.get(previousRankId);
                if (previousRank != null) {
                    boolean shouldExecute = isPromotion ? 
                        currentRank.getMinElo() > previousRank.getMinElo() :
                        currentRank.getMinElo() < previousRank.getMinElo();
                    
                    if (shouldExecute) {
                        if (isPromotion) {
                            executePromotionCommands(uuid, currentRank);
                        } else {
                            executeDemotionCommands(uuid, currentRank);
                        }
                        return true;
                    }
                }
            } else if (isPromotion) {
                // First time checking this player's rank - don't trigger promotion
                // This prevents promotion messages when players first join
                return false;
            }
        }
        
        return false;
    }
    
    private void executePromotionCommands(UUID uuid, Rank rank) {
        if (rank.getPromotionCommands().isEmpty()) {
            return;
        }
        
        // Execute commands with delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                for (String command : rank.getPromotionCommands()) {
                    executeCommand(player, command);
                }
            }
        }, commandDelay);
    }
    
    private void executeDemotionCommands(UUID uuid, Rank rank) {
        if (rank.getDemotionCommands().isEmpty()) {
            return;
        }
        
        // Retrieve the Player on the calling thread before scheduling
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        
        // Execute commands with delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (String command : rank.getDemotionCommands()) {
                executeCommand(player, command);
            }
        }, commandDelay);
    }
    
    public void checkOneTimeRewards(UUID uuid) {
        if (!enabled || !oneTimeRewardsEnabled) {
            return;
        }
        
        UserData userData = plugin.getUserManager().get(uuid);
        if (userData == null) {
            return;
        }
        
        Rank currentRank = calculateRank(userData.getTotalElo());
        Set<String> claimedRewards = oneTimeRewardsClaimed.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
        
        // Check if one-time rewards for this rank have been claimed
        if (!claimedRewards.contains(currentRank.getId()) && !currentRank.getOneTimeCommands().isEmpty()) {
            // Mark as claimed atomically
            if (claimedRewards.add(currentRank.getId())) {
                // Execute one-time commands
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        for (String command : currentRank.getOneTimeCommands()) {
                            executeCommand(player, command);
                        }
                    }
                }, commandDelay);
            }
        }
    }
    
    private void executeCommand(Player player, String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        // Replace placeholders
        command = command.replace("%player%", player.getName())
                        .replace("%uuid%", player.getUniqueId().toString());
        
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (Exception e) {
            Log.error("Failed to execute command: " + command, e);
        }
    }

    public void reload() {
        loadRanks();
        Log.info("Rank configuration reloaded");
    }
}
