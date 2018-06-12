package me.realized._duels.event;

import java.util.List;
import java.util.UUID;
import me.realized._duels.arena.Arena;
import org.bukkit.event.HandlerList;

public class MatchStartEvent extends MatchEvent {

    private static final HandlerList handlers = new HandlerList();

    private final boolean random;

    public MatchStartEvent(List<UUID> players, Arena arena, boolean random) {
        super(players, arena);
        this.random = random;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isRandomArena() {
        return random;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
