package me.realized._duels.event;

import me.realized._duels.arena.Arena;
import org.bukkit.event.Event;

import java.util.List;
import java.util.UUID;

public abstract class MatchEvent extends Event {

    private final List<UUID> players;
    private final Arena arena;

    public MatchEvent(List<UUID> players, Arena arena) {
        this.players = players;
        this.arena = arena;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public Arena getArena() {
        return arena;
    }
}
