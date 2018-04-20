package me.realized.duels.data;

import java.util.HashMap;
import java.util.Map;
import me.realized.duels.arena.Arena;
import me.realized.duels.cache.SettingCache;
import org.bukkit.Location;

public class ArenaData {

    private final String name;
    private final boolean disabled;
    private final Map<Integer, LocationData> positions = new HashMap<>();

    public ArenaData(final Arena arena) {
        this.name = arena.getName();
        this.disabled = arena.isDisabled();

        for (final Map.Entry<Integer, Location> entry : arena.getPositions().entrySet()) {
            positions.put(entry.getKey(), new LocationData(entry.getValue()));
        }
    }

    public Arena toArena(final SettingCache cache) {
        final Arena arena = new Arena(cache, name);
        arena.setDisabled(disabled);

        for (final Map.Entry<Integer, LocationData> entry : positions.entrySet()) {
            arena.setPosition(entry.getKey(), entry.getValue().toLocation());
        }

        return arena;
    }
}
