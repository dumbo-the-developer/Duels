package me.realized.duels.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.user.User;
import org.bukkit.entity.Player;

public class UserData implements User {

    @Getter
    private final UUID uuid;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private volatile int wins;
    @Getter
    @Setter
    private volatile int losses;
    @Setter
    private boolean requests = true;
    @Getter
    private ConcurrentHashMap<String, Integer> rating;
    private final List<MatchData> matches = new ArrayList<>();

    transient int defaultRating;
    transient int maxDisplayMatches;

    public UserData(final Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    public void addWin() {
        final int wins = this.wins;
        this.wins = wins + 1;
    }

    public void addLoss() {
        final int losses = this.losses;
        this.losses = losses + 1;
    }

    @Override
    public List<MatchData> getMatches() {
        return Collections.unmodifiableList(matches);
    }

    public boolean canRequest() {
        return requests;
    }

    @Override
    public int getRating(@Nonnull final Kit kit) {
        return rating != null ? rating.getOrDefault(name, defaultRating) : defaultRating;
    }

    public void setRating(final String name, final int rating) {
        if (this.rating == null) {
            this.rating = new ConcurrentHashMap<>();
        }

        this.rating.put(name, rating);
    }

    @Override
    public void resetRating(@Nonnull final Kit kit) {
        setRating(kit.getName(), defaultRating);
    }

    public void addMatch(final MatchData matchData) {
        if (matches.size() >= maxDisplayMatches) {
            matches.remove(0);
        }

        matches.add(matchData);
    }

    @Override
    public String toString() {
        return "UserData{" +
            "uuid=" + uuid +
            ", name='" + name + '\'' +
            ", wins=" + wins +
            ", losses=" + losses +
            ", requests=" + requests +
            ", matches=" + matches +
            ", rating=" + rating +
            '}';
    }
}
