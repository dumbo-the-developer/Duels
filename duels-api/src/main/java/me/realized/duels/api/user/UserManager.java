package me.realized.duels.api.user;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import me.realized.duels.api.kit.Kit;
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
        private final String type, identifier;
        @Getter
        private final List<TopData> data;

        public TopEntry(final String type, final String identifier, final List<TopData> data) {
            this.creation = System.currentTimeMillis();
            this.type = type;
            this.identifier = identifier;
            this.data = data;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            final TopEntry topEntry = (TopEntry) other;
            return Objects.equals(type, topEntry.type) && Objects.equals(identifier, topEntry.identifier) && Objects.equals(data, topEntry.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, identifier, data);
        }
    }

    class TopData implements Comparable<TopData> {

        @Getter
        private final UUID uuid;
        @Getter
        private final String name;
        @Getter
        private final int value;

        public TopData(final UUID uuid, final String name, final int value) {
            this.uuid = uuid;
            this.name = name;
            this.value = value;
        }

        @Override
        public int compareTo(@Nonnull final TopData data) {
            Objects.requireNonNull(data, "data");
            return Integer.compare(value, data.value);
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            final TopData topData = (TopData) other;
            return value == topData.value && Objects.equals(uuid, topData.uuid) && Objects.equals(name, topData.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, name, value);
        }
    }
}
