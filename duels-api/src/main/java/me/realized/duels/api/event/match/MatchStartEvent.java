package me.realized.duels.api.event.match;

import javax.annotation.Nonnull;
import lombok.Getter;
import me.realized.duels.api.match.Match;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Called when a Match has started.
 */
public class MatchStartEvent extends MatchEvent {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Player[] players;

    public MatchStartEvent(final Match match, @Nonnull final Player... players) {
        super(match);
        this.players = players;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
