package me.realized.duels.queue;

import java.util.Objects;
import lombok.Getter;
import me.realized.duels.setting.CachedInfo;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class QueueEntry {

    @Getter
    private final Player player;
    @Getter
    private final CachedInfo info;

    QueueEntry(final Player player, final Location location, final String duelzone) {
        this.player = player;
        this.info = new CachedInfo(location, duelzone);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final QueueEntry that = (QueueEntry) other;
        return Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }
}
