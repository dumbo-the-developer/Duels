package me.realized.duels.event;

import me.realized.duels.arena.Arena;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.UUID;

public class MatchStartEvent extends MatchEvent {

    private static final HandlerList handlers = new HandlerList();

    private final boolean random;

    public MatchStartEvent(List<UUID> players, Arena arena, boolean random) {
        super(players, arena);
        this.random = random;
    }

    public boolean isRandomArena() {
        return random;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
