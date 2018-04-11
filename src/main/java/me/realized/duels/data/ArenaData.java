package me.realized.duels.data;

import java.util.HashMap;
import java.util.Map;

public class ArenaData {

    private final String name;
    private final boolean disabled;
    private final Map<Integer, LocationData> positions = new HashMap<>();

    public ArenaData(final String name, final boolean disabled) {
        this.name = name;
        this.disabled = disabled;
    }
}
