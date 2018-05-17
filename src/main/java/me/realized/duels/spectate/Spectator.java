package me.realized.duels.spectate;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import me.realized.duels.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Spectator {

    @Getter
    private final UUID owner;
    @Getter
    private final Location origin;
    @Getter
    private final String targetName;
    @Getter
    private final Arena arena;

    Spectator(final Player owner, final Player target, final Arena arena) {
        this.owner = owner.getUniqueId();
        this.origin = owner.getLocation().clone();
        this.targetName = target.getName();
        this.arena = arena;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final Spectator spectator = (Spectator) other;
        return Objects.equals(owner, spectator.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner);
    }
}
