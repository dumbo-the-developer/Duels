package me.realized.duels.api.user;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.util.Pair;
import org.bukkit.entity.Player;

public interface UserManager {


    /**
     * @return true if all users have completed loading to the memory, otherwise false
     */
    boolean isLoaded();


    /**
     * If {@link #isLoaded()} returns false, this may return null even if userdata file exists.
     *
     * @param name Name of the user to get
     * @return User with the given name if exists, otherwise null
     */
    @Nullable
    User get(@Nonnull final String name);


    /**
     * If {@link #isLoaded()} returns false, this may return null even if userdata file exists.
     *
     * @param uuid UUID of the user to get
     * @return User with the given UUID if exists, otherwise null
     */
    @Nullable
    User get(@Nonnull final UUID uuid);


    /**
     * Calls {@link #get(UUID)} with {@link Player#getUniqueId()}.
     *
     * @see #get(UUID)
     */
    @Nullable
    User get(@Nonnull final Player player);


    /**
     * Method is thread-safe.
     *
     * @return TopEntry containing name and wins of the top 10 Wins or null if the leaderboard has not loaded yet
     */
    @Nullable
    TopEntry getTopWins();


    /**
     * Method is thread-safe.
     *
     * @return TopEntry containing name and losses of the top 10 Losses or null if the leaderboard has not loaded yet
     */
    @Nullable
    TopEntry getTopLosses();


    /**
     * Method is thread-safe.
     *
     * @param kit Kit to get TopEntry
     * @return TopEntry containing name and rating of the top 10 Rating for kit or null if the leaderboard has not loaded yet
     */
    @Nullable
    TopEntry getTopRatings(@Nonnull final Kit kit);


    class TopEntry {

        @Getter
        private final long creation;
        @Getter
        private final String name, type;
        @Getter
        private final List<Pair<String, Integer>> data;

        public TopEntry(final String name, final String type, final List<Pair<String, Integer>> data) {
            this.creation = System.currentTimeMillis();
            this.name = name;
            this.type = type;
            this.data = data;
        }
    }
}
