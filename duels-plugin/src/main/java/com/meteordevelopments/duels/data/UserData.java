package com.meteordevelopments.duels.data;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.api.user.MatchInfo;
import com.meteordevelopments.duels.api.user.User;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserData implements User {

    private static transient final String ERROR_USER_SAVE = "An error occured while saving userdata of %s!";
    transient File folder;
    transient int defaultRating;
    transient int matchesToDisplay;
    @Getter
    private UUID uuid;
    @Getter
    @Setter
    private String name;
    @Getter
    private volatile int wins;
    @Getter
    private volatile int losses;
    private boolean requests = true;
    private ConcurrentHashMap<String, Integer> rating;
    private List<MatchData> matches = new ArrayList<>();
    private boolean partyRequests = true;

    private UserData() {
    }

    public UserData(final File folder, final int defaultRating, final int matchesToDisplay, final Player player) {
        this.folder = folder;
        this.defaultRating = defaultRating;
        this.matchesToDisplay = matchesToDisplay;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    @Override
    public void setWins(final int wins) {
        this.wins = wins;

        if (isOffline()) {
            trySave();
        }
    }

    @Override
    public void setLosses(final int losses) {
        this.losses = losses;

        if (isOffline()) {
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

        if (isOffline()) {
            trySave();
        }
    }

    @NotNull
    @Override
    public List<MatchInfo> getMatches() {
        return Collections.unmodifiableList(matches);
    }

    @Override
    public int getRating() {
        return getRatingUnsafe(null);
    }

    @Override
    public int getRating(@NotNull final Kit kit) {
        Objects.requireNonNull(kit, "kit");
        return getRatingUnsafe(kit);
    }

    @Override
    public void resetRating() {
        setRating(null, defaultRating);
    }

    @Override
    public void resetRating(@NotNull final Kit kit) {
        Objects.requireNonNull(kit, "kit");
        setRating(kit, defaultRating);
    }

    @Override
    public void reset() {
        wins = 0;
        losses = 0;
        matches.clear();
        rating.clear();

        if (isOffline()) {
            trySave();
        }
    }

    public boolean canPartyRequest() {
        return partyRequests;
    }

    public void setPartyRequests(final boolean partyRequests) {
        this.partyRequests = partyRequests;

        if (isOffline()) {
            trySave();
        }
    }

    public int getRatingUnsafe(@Nullable final Kit kit) {
        return this.rating != null ? this.rating.getOrDefault(kit == null ? "-" : kit.getName(), defaultRating) : defaultRating;
    }

    public void setRating(final Kit kit, final int rating) {
        if (this.rating == null) {
            this.rating = new ConcurrentHashMap<>();
        }

        this.rating.put(kit == null ? "-" : kit.getName(), rating);

        if (isOffline()) {
            trySave();
        }
    }

    private boolean isOffline() {
        return Bukkit.getPlayer(uuid) == null;
    }

    public void addWin() {
        final int wins = this.wins;
        this.wins = wins + 1;
    }

    public void addLoss() {
        final int losses = this.losses;
        this.losses = losses + 1;
    }

    public void addMatch(final MatchData matchData) {
        if (!matches.isEmpty() && matches.size() >= matchesToDisplay) {
            matches.removeFirst();
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

            try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
                JsonUtil.getObjectWriter().writeValue(writer, this);
                writer.flush();
            }
        } catch (IOException ex) {
            Log.error(String.format(ERROR_USER_SAVE, name), ex);
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
