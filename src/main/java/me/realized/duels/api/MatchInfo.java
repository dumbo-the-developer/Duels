package me.realized.duels.api;

public interface MatchInfo {

    String getWinner();

    String getLoser();

    long getTime();

    int getDuration();

    double getHealth();
}
