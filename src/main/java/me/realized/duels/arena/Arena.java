package me.realized.duels.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

public class Arena {

    @Getter
    private final String name;
    private final Map<Integer, Location> positions = new HashMap<>();
    private final List<UUID> players = new ArrayList<>();
    @Getter
    @Setter
    private boolean disabled, used;

    public Arena(final String name) {
        this.name = name;
    }



    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final Arena arena = (Arena) other;
        return Objects.equals(name, arena.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
