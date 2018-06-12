package me.realized.duels.api.user;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import org.bukkit.entity.Player;

public interface UserManager {

    /**
     * @param uuid UUID to search through the user Map
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
     * @return List of name and wins for the top 10 on the wins leaderboard or null if the leaderboard is loading/updating
     */
    List<SortedEntry<String, Integer>> getTopWins();


    /**
     * Method is thread-safe.
     *
     * @return List of name and wins for the top 10 on the losses leaderboard or null if the leaderboard is loading/updating
     */
    List<SortedEntry<String, Integer>> getTopLosses();


    /**
     * Calling this method will iterate through all the users, collect them in a list as a new instance of SortedEntry, and then sort.
     * Refrain from calling this method on the main server thread to prevent blocking.
     *
     * Method is thread-safe.
     *
     * @param function Function to retrieve the Comparable value from the users
     * @return List of SortedEntry with User's name and the value from function, ordered in reverse order
     */
    <V extends Comparable<V>> List<SortedEntry<String, V>> sorted(@Nonnull final Function<User, V> function);


    class SortedEntry<K, V extends Comparable<V>> implements Comparable<SortedEntry<K, V>> {

        @Getter
        private final K key;
        @Getter
        private final V value;

        public SortedEntry(@Nonnull final K key, @Nonnull final V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(@Nonnull final SortedEntry<K, V> other) {
            return value.compareTo(other.value);
        }

        @Override
        public String toString() {
            return "SortedEntry{" + "key=" + key + ", value=" + value + '}';
        }
    }
}
