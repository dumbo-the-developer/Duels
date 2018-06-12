package me.realized._duels.event;

import java.util.List;
import java.util.UUID;
import me.realized._duels.arena.Arena;
import org.bukkit.event.HandlerList;

public class MatchEndEvent extends MatchEvent {

    private static final HandlerList handlers = new HandlerList();

    private final EndReason reason;

    public MatchEndEvent(List<UUID> players, Arena arena, EndReason reason) {
        super(players, arena);
        this.reason = reason;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public EndReason getReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public enum EndReason {

        OPPONENT_QUIT,
        OPPONENT_DEFEAT,
        PLUGIN_DISABLE,
        MAX_TIME_REACHED,
        OTHER
    }
}
