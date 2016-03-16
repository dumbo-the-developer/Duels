package me.realized.duels.data;

public class MatchData {

    private final String winner;
    private final String loser;
    private final long time;
    private final int duration;
    private final double health;

    public MatchData(String winner, String loser, long time, int duration, double health) {
        this.winner = winner;
        this.loser = loser;
        this.time = time;
        this.duration = duration;
        this.health = health;
    }

    public String getWinner() {
        return winner;
    }

    public String getLoser() {
        return loser;
    }

    public long getTime() {
        return time;
    }

    public int getDuration() {
        return duration;
    }

    public double getHealth() {
        return health;
    }
}
