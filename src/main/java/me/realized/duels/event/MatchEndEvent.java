package me.realized.duels.event;

import me.realized.duels.arena.Arena;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.UUID;

public class MatchEndEvent extends MatchEvent {

    private static final HandlerList handlers = new HandlerList();

    public MatchEndEvent(List<UUID> players, Arena arena) {
        super(players, arena);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
