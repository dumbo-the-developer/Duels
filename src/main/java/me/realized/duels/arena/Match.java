package me.realized.duels.arena;

public class Match {

    private final long creation;
    private String kitName;

    Match() {
        this.creation = System.currentTimeMillis();
    }

    public enum EndReason {

        OPPONENT_QUIT, OPPONENT_DEFEAT, PLUGIN_DISABLE, MAX_TIME_REACHED, OTHER
    }
}
