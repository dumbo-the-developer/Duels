package me.realized.duels.data;

import me.realized.duels.arena.Arena;
import me.realized.duels.utilities.location.SimpleLocation;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class ArenaData {

    private final String name;
    private final boolean disabled;
    private final Map<Integer, SimpleLocation> positions = new HashMap<>();

    public ArenaData(Arena arena) {
        this.name = arena.getName();
        this.disabled = arena.isDisabled();

        for (Map.Entry<Integer, Location> entry : arena.getPositions().entrySet()) {
            if (entry.getValue().getWorld() != null) {
                positions.put(entry.getKey(), new SimpleLocation(entry.getValue()));
            }
        }
    }

    public Arena toArena() {
        Arena arena = new Arena(name, disabled);

        for (Map.Entry<Integer, SimpleLocation> entry : positions.entrySet()) {
            arena.addPosition(entry.getKey(), entry.getValue().toLocation());
        }

        return arena;
    }
}
