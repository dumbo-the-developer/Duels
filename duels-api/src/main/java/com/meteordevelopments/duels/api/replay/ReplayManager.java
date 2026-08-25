package com.meteordevelopments.duels.api.replay;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ReplayManager {

    /**
     * Starts replay playback for a player.
     *
     * @param watcher the player who will watch the replay
     * @param replayId the ID of the replay to play
     */
    void playReplay(@NotNull Player watcher, @NotNull String replayId);

    /**
     * Deletes a recorded replay by its ID.
     *
     * @param replayId the ID of the replay to delete
     */
    void deleteReplay(@NotNull String replayId);

    /**
     * Checks if a player is currently watching a replay.
     *
     * @param player the player to check
     * @return true if the player is in an active replay session, false otherwise
     */
    boolean isWatching(@NotNull Player player);

    /**
     * Checks if the replay system is available and enabled (e.g. required dependencies like ProtocolLib are present).
     *
     * @return true if the replay system is available, false otherwise
     */
    boolean isAvailable();
}

