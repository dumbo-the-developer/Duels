package me.realized.duels.setting;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

public class CachedInfo {

    @Getter
    @Setter
    private Location location;
    @Getter
    @Setter
    private String duelzone;

    public CachedInfo(final Location location, final String duelzone) {
        this.location = location;
        this.duelzone = duelzone;
    }

    CachedInfo() {
        this(null, null);
    }
}
