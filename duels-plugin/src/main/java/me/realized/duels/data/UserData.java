package me.realized.duels.data;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    transient int matchesToDisplay;

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
        Objects.requireNonNull(kit, "kit");
        return this.rating != null ? this.rating.getOrDefault(kit.getName(), defaultRating) : defaultRating;
    }

    public void setRating(final String name, final int rating) {
        if (this.rating == null) {
            this.rating = new ConcurrentHashMap<>();
        }

        this.rating.put(name, rating);
    }

    public void reset() {
        setWins(0);
        setLosses(0);
        matches.clear();
        rating.clear();
    }

    @Override
    public void resetRating(@Nonnull final Kit kit) {
        Objects.requireNonNull(kit, "kit");
        setRating(kit.getName(), defaultRating);
    }

    public void addMatch(final MatchData matchData) {
        if (matches.size() >= matchesToDisplay) {
            matches.remove(0);
        }

        matches.add(matchData);
    }

    void refreshMatches() {
        if (matches.size() < matchesToDisplay) {
            return;
        }

        final List<MatchData> division = Lists.newArrayList(matches.subList(matches.size() - matchesToDisplay, matches.size()));
        matches.clear();
        matches.addAll(division);
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
