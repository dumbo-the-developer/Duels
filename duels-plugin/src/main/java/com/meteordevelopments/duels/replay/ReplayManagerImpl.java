package com.meteordevelopments.duels.replay;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.event.match.MatchEndEvent;
import com.meteordevelopments.duels.api.event.match.MatchStartEvent;
import com.meteordevelopments.duels.api.match.Match;
import com.meteordevelopments.duels.core.match.DuelMatch;
import com.meteordevelopments.duels.replay.config.ConfigManager;
import com.meteordevelopments.duels.replay.data.DuelReplayMetadata;
import com.meteordevelopments.duels.replay.listener.ReplayListener;
import com.meteordevelopments.duels.replay.playback.ReplayHelper;
import com.meteordevelopments.duels.replay.playback.Replayer;
import com.meteordevelopments.duels.replay.storage.DefaultReplaySaver;
import com.meteordevelopments.duels.replay.storage.ReplayCleanup;
import com.meteordevelopments.duels.replay.storage.ReplaySaver;
import com.meteordevelopments.duels.replay.util.LogUtils;
import com.meteordevelopments.duels.replay.util.ProtocolLibUtil;
import com.meteordevelopments.duels.replay.util.StringUtils;
import com.meteordevelopments.duels.replay.util.VersionUtil;
import com.meteordevelopments.duels.api.replay.ReplayManager;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Reloadable;

public class ReplayManagerImpl implements ReplayManager, Loadable, Reloadable, Listener {

    public static final Map<String, Replay> activeReplays = new ConcurrentHashMap<>();
    private final Map<UUID, Replay> activeDuelRecordings = new ConcurrentHashMap<>();

    private final DuelsPlugin plugin;
    private boolean available = false;
    private DefaultReplaySaver storage;
    private ReplayListener replayListener;

    public ReplayManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleLoad() {
        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            this.available = false;
            plugin.warn("[Replay] ProtocolLib is missing or disabled! The replay system will be disabled.");
            return;
        }

        this.available = true;
        ConfigManager.loadConfigs();

        // Restore any spectators from prior server crashes/restarts
        com.meteordevelopments.duels.replay.playback.session.ReplaySpectatorStorage.checkAndRestoreAllOnline();

        this.storage = new DefaultReplaySaver();
        ReplaySaver.register(storage);

        this.replayListener = new ReplayListener();
        this.replayListener.register();

        DuelsPlugin.getFoliaLib().getScheduler().runAsync(task -> {
            if (VersionUtil.isAbove(VersionUtil.VersionEnum.V1_21)) {
                try {
                    ProtocolLibUtil.prepare();
                } catch (Throwable t) {
                    plugin.warn("[Replay] Failed to prepare ProtocolLib packet wrappers: " + t.getMessage());
                }
            }
            if (ConfigManager.CLEANUP_REPLAYS > 0) {
                ReplayCleanup.cleanupReplays();
            }
        });
    }

    @Override
    public void handleUnload() {
        if (!available) {
            return;
        }

        // Stop all active recordings
        for (Replay replay : new ArrayList<>(activeReplays.values())) {
            try {
                if (replay.isRecording() && replay.getRecorder() != null && replay.getRecorder().getData().getActions().size() > 0) {
                    replay.getRecorder().stop(ConfigManager.SAVE_STOP);
                }
            } catch (Exception e) {
                LogUtils.log("Error stopping recording on unload: " + e.getMessage());
            }
        }

        // Stop all active replay viewers
        for (Replayer replayer : new ArrayList<>(ReplayHelper.replaySessions.values())) {
            try {
                replayer.stop();
                replayer.getSession().resetPlayer();
            } catch (Exception e) {
                LogUtils.log("Error stopping playback session on unload: " + e.getMessage());
            }
        }

        activeReplays.clear();
        activeDuelRecordings.clear();
        ReplayHelper.replaySessions.clear();

        if (replayListener != null) {
            replayListener.unregister();
        }
    }

    public void handleReload() {
        if (!available) {
            return;
        }
        ConfigManager.reloadConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMatchStart(final MatchStartEvent event) {
        if (!available || !ConfigManager.ENABLED) return;

        final Match match = event.getMatch();
        final Player[] players = event.getPlayers();
        if (players == null || players.length < 2) return;

        try {
            final String replayId = StringUtils.getRandomString(6);
            final Replay replay = new Replay(replayId, new com.meteordevelopments.duels.replay.data.ReplayData());

            final DuelReplayMetadata metadata = new DuelReplayMetadata(replayId);
            metadata.setPlayer1Uuid(players[0].getUniqueId());
            metadata.setPlayer1Name(players[0].getName());
            metadata.setPlayer2Uuid(players[1].getUniqueId());
            metadata.setPlayer2Name(players[1].getName());

            if (match.getArena() != null) {
                metadata.setArenaName(match.getArena().getName());
            }

            if (match instanceof DuelMatch duelMatch) {
                if (duelMatch.getCustomKitSnapshot() != null) {
                    metadata.setKitName("[Custom] " + duelMatch.getCustomKitSnapshot().getName());
                } else if (duelMatch.getKit() != null) {
                    metadata.setKitName(duelMatch.getKit().getName());
                } else {
                    metadata.setKitName("Own Inventory");
                }
            } else if (match.getKit() != null) {
                metadata.setKitName(match.getKit().getName());
            } else {
                metadata.setKitName("Own Inventory");
            }

            metadata.setBetAmount(match.getBet());
            metadata.setStartTime(match.getStart());

            replay.setMetadata(metadata);
            replay.recordAll(Arrays.asList(players), Bukkit.getConsoleSender());

            for (Player player : players) {
                activeDuelRecordings.put(player.getUniqueId(), replay);
            }
        } catch (Throwable t) {
            LogUtils.log("Failed to start duel replay recording: " + t.getMessage());
            t.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchEnd(final MatchEndEvent event) {
        if (!available) return;

        final Match match = event.getMatch();
        final UUID winner = event.getWinner();
        final UUID loser = event.getLoser();
        final MatchEndEvent.Reason reason = event.getReason();

        try {
            Replay replay = null;
            for (Player player : match.getStartingPlayers()) {
                replay = activeDuelRecordings.remove(player.getUniqueId());
                if (replay != null) break;
            }

            if (replay == null || !replay.isRecording() || replay.getRecorder() == null) {
                return;
            }

            final DuelReplayMetadata metadata = replay.getMetadata();
            if (metadata != null) {
                metadata.setEndTime(System.currentTimeMillis());
                metadata.setDurationMillis(metadata.getEndTime() - metadata.getStartTime());
                metadata.setDurationTicks(replay.getRecorder().getCurrentTick());
                metadata.setEndReason(reason != null ? reason.name() : "OTHER");

                if (winner != null) {
                    metadata.setWinnerUuid(winner);
                    Player winnerPlayer = Bukkit.getPlayer(winner);
                    metadata.setWinnerName(winnerPlayer != null ? winnerPlayer.getName() : (winner.equals(metadata.getPlayer1Uuid()) ? metadata.getPlayer1Name() : metadata.getPlayer2Name()));
                }

                if (loser != null) {
                    metadata.setLoserUuid(loser);
                    Player loserPlayer = Bukkit.getPlayer(loser);
                    metadata.setLoserName(loserPlayer != null ? loserPlayer.getName() : (loser.equals(metadata.getPlayer1Uuid()) ? metadata.getPlayer1Name() : metadata.getPlayer2Name()));
                }
            }

            replay.getRecorder().stop(ConfigManager.SAVE_STOP);

            // Clean up player mapping
            for (Player player : match.getStartingPlayers()) {
                activeDuelRecordings.remove(player.getUniqueId());
            }
        } catch (Throwable t) {
            LogUtils.log("Error finalizing duel replay recording: " + t.getMessage());
            t.printStackTrace();
        }
    }

    @Override
    public void playReplay(@org.jetbrains.annotations.NotNull final Player watcher, @org.jetbrains.annotations.NotNull final String replayId) {
        if (watcher == null || replayId == null) return;

        if (!available) {
            plugin.getLang().sendMessage(watcher, "REPLAY.protocollib-missing");
            return;
        }

        if (ReplayHelper.replaySessions.containsKey(watcher.getName())) {
            plugin.getLang().sendMessage(watcher, "REPLAY.already-watching");
            return;
        }

        ReplaySaver.load(replayId, replay -> {
            if (replay == null) {
                plugin.getLang().sendMessage(watcher, "REPLAY.not-found", "id", replayId);
                return;
            }

            DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(watcher, task -> {
                replay.play(watcher);
            });
        });
    }

    @Override
    public void deleteReplay(@org.jetbrains.annotations.NotNull final String replayId) {
        if (!available) return;
        if (replayId != null) {
            ReplaySaver.delete(replayId);
        }
    }

    public DefaultReplaySaver getStorage() {
        return storage;
    }

    public DuelReplayMetadata getMetadata(final String replayId) {
        return storage != null ? storage.getMetadata(replayId) : null;
    }

    public List<DuelReplayMetadata> getAllMetadata() {
        return storage != null ? storage.getAllMetadata() : Collections.emptyList();
    }

    public List<DuelReplayMetadata> getMetadataForPlayer(final UUID uuid) {
        return storage != null ? storage.getMetadataForPlayer(uuid) : Collections.emptyList();
    }

    @Override
    public boolean isWatching(@org.jetbrains.annotations.NotNull final Player player) {
        return available && player != null && ReplayHelper.replaySessions.containsKey(player.getName());
    }

    public Replayer getReplayer(final Player player) {
        return available && player != null ? ReplayHelper.replaySessions.get(player.getName()) : null;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}

