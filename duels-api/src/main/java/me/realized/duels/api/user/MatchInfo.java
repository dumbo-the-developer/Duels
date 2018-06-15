package me.realized.duels.api.user;

import java.util.GregorianCalendar;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
     * @return Name of the kit used in this match or null if useOwnInventory was enabled for this match
     */
    @Nullable
    String getKit();


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
