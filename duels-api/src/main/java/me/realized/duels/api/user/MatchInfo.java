package me.realized.duels.api.user;

import java.util.GregorianCalendar;
import javax.annotation.Nonnull;

public interface MatchInfo {

    /**
     * @return Name of the winner of this match
     */
    @Nonnull
    String getWinner();


    /**
     * @return Name of the loser of this match
     */
    @Nonnull
    String getLoser();


    /**
     * Uses {@link GregorianCalendar#getTimeInMillis()}.
     *
     * @return created date of this match info in milliseconds.
     */
    long getCreation();


    /**
     * @return Duration of this match in milliseconds
     */
    long getDuration();


    /**
     * @return Winner's finishing health
     */
    double getHealth();
}
