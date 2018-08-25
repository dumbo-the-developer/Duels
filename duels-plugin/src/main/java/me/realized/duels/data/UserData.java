package me.realized.duels.data;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.user.User;
import me.realized.duels.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UserData implements User {

    @Getter
    private final UUID uuid;
    @Getter
    @Setter
    private String name;
    @Getter
    private volatile int wins;
    @Getter
    private volatile int losses;
    private boolean requests = true;
    @Getter
    private ConcurrentHashMap<String, Integer> rating;
    private final List<MatchData> matches = new ArrayList<>();

    transient DuelsPlugin plugin;
    transient File folder;
    transient int defaultRating;
    transient int matchesToDisplay;

    public UserData(final DuelsPlugin plugin, final File folder, final int defaultRating, final int matchesToDisplay, final Player player) {
        this.plugin = plugin;
        this.folder = folder;
        this.defaultRating = defaultRating;
        this.matchesToDisplay = matchesToDisplay;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    @Override
    public void setWins(final int wins) {
        this.wins = wins;

        if (!isOnline()) {
            trySave();
        }
    }

    @Override
    public void setLosses(final int losses) {
        this.losses = losses;

        if (!isOnline()) {
            trySave();
        }
    }

    @Override
    public boolean canRequest() {
        return requests;
    }

    @Override
    public void setRequests(final boolean requests) {
        this.requests = requests;

        if (!isOnline()) {
            trySave();
        }
    }

    @Nonnull
    @Override
    public List<MatchData> getMatches() {
        return Collections.unmodifiableList(matches);
    }

    @Override
    public int getRating(@Nonnull final Kit kit) {
        Objects.requireNonNull(kit, "kit");
        return this.rating != null ? this.rating.getOrDefault(kit.getName(), defaultRating) : defaultRating;
    }

    @Override
    public void resetRating(@Nonnull final Kit kit) {
        Objects.requireNonNull(kit, "kit");
        setRating(kit.getName(), defaultRating);
    }

    @Override
    public void reset() {
        wins = 0;
        losses = 0;
        matches.clear();
        rating.clear();

        if (!isOnline()) {
            trySave();
        }
    }

    private boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

    public void addWin() {
        final int wins = this.wins;
        this.wins = wins + 1;
    }

    public void addLoss() {
        final int losses = this.losses;
        this.losses = losses + 1;
    }

    public void setRating(final String name, final int rating) {
        if (this.rating == null) {
            this.rating = new ConcurrentHashMap<>();
        }

        this.rating.put(name, rating);

        if (!isOnline()) {
            trySave();
        }
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

    public void trySave() {
        final File file = new File(folder, uuid + ".json");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
                plugin.getGson().toJson(this, writer);
                writer.flush();
            }
        } catch (IOException ex) {
            Log.error("An error occured while saving userdata of " + name + "!", ex);
        }
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
