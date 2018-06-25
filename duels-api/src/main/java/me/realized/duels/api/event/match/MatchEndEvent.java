package me.realized.duels.api.event.match;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import me.realized.duels.api.match.Match;
import org.bukkit.event.HandlerList;

/**
 * Called when a Match is ending.
 */
public class MatchEndEvent extends MatchEvent {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final UUID winner, loser;
    @Getter
    private final Reason reason;

    public MatchEndEvent(final Match match, @Nullable final UUID winner, @Nullable final UUID loser, @Nonnull final Reason reason) {
        super(match);
        this.winner = winner;
        this.loser = loser;
        this.reason = reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum Reason {

        OPPONENT_DEFEAT,
        TIE,
        MAX_TIME_REACHED,
        PLUGIN_DISABLE,
        OTHER
    }
}
