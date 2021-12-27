package me.realized.duels.api.user;

import java.util.GregorianCalendar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the data of a match that had occured in the past.
 */
public interface MatchInfo {

    /**
     * The name of the winner of this match.
     *
     * @return Never-null name of the winner of this match.
     */
    @NotNull
    String getWinner();


    /**
     * The name of the loser of this match.
     *
     * @return Never-null name of the loser of this match.
     */
    @NotNull
    String getLoser();


    /**
     * The name of the kit used in this match or null if no kit was used.
     *
     * @return Name of the kit used in this match or null if no kit was used.
     */
    @Nullable
    String getKit();


    /**
     * The created timestamp of this match info in milliseconds.
     * Note: Uses {@link GregorianCalendar#getTimeInMillis()}.
     *
     * @return created timestamp of this match info in milliseconds.
     */
    long getCreation();


    /**
     * The duration of this match in milliseconds.
     *
     * @return Duration of this match in milliseconds.
     */
    long getDuration();


    /**
     * The winner's finishing health.
     *
     * @return Winner's finishing health.
     */
    double getHealth();
}
