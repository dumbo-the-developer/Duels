package me.realized.duels.spectate;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import me.realized.duels.api.spectate.Spectator;
import me.realized.duels.arena.ArenaImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class SpectatorImpl implements Spectator {

    @Getter
    private final UUID uuid;
    @Getter
    private final UUID targetUuid;
    @Getter
    private final String targetName;
    @Getter
    private final ArenaImpl arena;

    SpectatorImpl(final Player owner, final Player target, final ArenaImpl arena) {
        this.uuid = owner.getUniqueId();
        this.targetUuid = target.getUniqueId();
        this.targetName = target.getName();
        this.arena = arena;
    }

    @Nullable
    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Nullable
    @Override
    public Player getTarget() {
        return Bukkit.getPlayer(targetUuid);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final SpectatorImpl spectator = (SpectatorImpl) other;
        return Objects.equals(uuid, spectator.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
