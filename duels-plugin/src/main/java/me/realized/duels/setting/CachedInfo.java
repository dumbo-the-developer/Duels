package me.realized.duels.setting;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;

public class CachedInfo {

    @Getter
    @Setter
    private Location location;
    @Getter
    @Setter
    private String duelzone;
    @Getter
    @Setter
    private GameMode gameMode;

    public CachedInfo(final Location location, final String duelzone, final GameMode gameMode) {
        this.location = location;
        this.duelzone = duelzone;
        this.gameMode = gameMode;
    }

    CachedInfo() {
        this(null, null, null);
    }
}
