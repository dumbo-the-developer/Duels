package me.realized.duels.api.user;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import me.realized.duels.api.kit.Kit;

public interface User {

    /**
     * @return UUID of this user
     */
    @Nonnull
    UUID getUuid();


    /**
     * This value is updated on login.
     * Method is thread-safe.
     *
     * @return Name of this user
     */
    @Nonnull
    String getName();


    /**
     * Method is thread-safe.
     *
     * @return total wins of this user
     */
    int getWins();


    /**
     * Sets new total wins for this user
     */
    void setWins(final int wins);


    /**
     * Method is thread-safe.
     *
     * @return total losses of this user
     */
    int getLosses();


    /**
     * Sets new total wins for this user
     */
    void setLosses(final int losses);


    /**
     * @return true if this user has requests enabled, otherwise false
     */
    boolean canRequest();


    /**
     * Method is thread-safe.
     *
     * @param kit Kit to check for rating
     * @return Rating for this kit or the default rating specified in the configuration
     */
    int getRating(@Nonnull final Kit kit);


    /**
     * Method is thread-safe.
     *
     * @param kit Kit to reset the rating to default.
     */
    void resetRating(@Nonnull final Kit kit);


    /**
     * @return List of recent matches for this user
     */
    List<? extends MatchInfo> getMatches();
}
