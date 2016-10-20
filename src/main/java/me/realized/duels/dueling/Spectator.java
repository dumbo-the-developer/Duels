package me.realized.duels.dueling;

import me.realized.duels.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

class Spectator {

    private final UUID owner;
    private final String spectating;
    private final Location base;
    private final Arena target;

    public Spectator(Player base, String spectating, Arena target) {
        this.owner = base.getUniqueId();
        this.spectating = spectating;
        this.base = base.getLocation().clone();
        this.target = target;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getSpectatingName() {
        return spectating;
    }

    public Location getBase() {
        return base;
    }

    public Arena getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Spectator spectator = (Spectator) o;

        return owner != null ? owner.equals(spectator.owner) : spectator.owner == null;
    }

    @Override
    public int hashCode() {
        return owner != null ? owner.hashCode() : 0;
    }
}
