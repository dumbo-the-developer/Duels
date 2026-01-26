package com.meteordevelopments.duels.queue;

import lombok.Getter;
import com.meteordevelopments.duels.setting.CachedInfo;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

public class QueueEntry {

    @Getter
    private final Player player;
    @Getter
    private final CachedInfo info;

    QueueEntry(final Player player, final Location location, final String duelZone) {
        this.player = player;
        this.info = new CachedInfo(location, duelZone);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final QueueEntry that = (QueueEntry) other;
        return Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }
}
