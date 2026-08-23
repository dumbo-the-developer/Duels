package com.meteordevelopments.duels.replay.data;

import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Encapsulates all Duels-specific metadata for a recorded duel replay.
 */
@Getter
@Setter
public class DuelReplayMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private String replayId;

    private UUID player1Uuid;
    private String player1Name;

    private UUID player2Uuid;
    private String player2Name;

    private String kitName;
    private String arenaName;

    private UUID winnerUuid;
    private String winnerName;

    private UUID loserUuid;
    private String loserName;

    private String endReason; // OPPONENT_DEFEAT, TIE, MAX_TIME_REACHED, FORFEIT, DISCONNECT, etc.

    private long startTime;
    private long endTime;
    private long durationMillis;
    private int durationTicks;

    private int betAmount;

    public DuelReplayMetadata() {
    }

    public DuelReplayMetadata(final String replayId) {
        this.replayId = replayId;
        this.startTime = System.currentTimeMillis();
    }

    public boolean involvesPlayer(final UUID uuid) {
        if (uuid == null) return false;
        return uuid.equals(player1Uuid) || uuid.equals(player2Uuid);
    }

    public boolean involvesPlayer(final String name) {
        if (name == null) return false;
        return name.equalsIgnoreCase(player1Name) || name.equalsIgnoreCase(player2Name);
    }

    public String getFormattedDuration() {
        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
