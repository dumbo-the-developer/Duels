package me.realized.duels.data;

import lombok.Getter;
import me.realized.duels.api.MatchInfo;

public class MatchData implements MatchInfo {

    @Getter
    private final String winner;
    @Getter
    private final String loser;
    @Getter
    private final long time;
    @Getter
    private final long duration;
    @Getter
    private final double health;

    public MatchData(final String winner, final String loser, final long time, final long duration, final double health) {
        this.winner = winner;
        this.loser = loser;
        this.time = time;
        this.duration = duration;
        this.health = health;
    }
}
