package me.realized.duels.api.event.match;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.realized.duels.api.match.Match;
import org.bukkit.event.HandlerList;

/**
 * Called when a {@link Match} is ending.
 */
public class MatchEndEvent extends MatchEvent {

    private static final HandlerList handlers = new HandlerList();

    private final UUID winner, loser;
    private final Reason reason;

    public MatchEndEvent(@Nonnull final Match match, @Nullable final UUID winner, @Nullable final UUID loser, @Nonnull final Reason reason) {
        super(match);
        Objects.requireNonNull(reason, "reason");
        this.winner = winner;
        this.loser = loser;
        this.reason = reason;
    }

    /**
     * Winner of the {@link Match}. May be null if there was a no winner!
     *
     * @return {@link UUID} of the winner of the {@link Match}. May be null if there was a no winner!
     */
    @Nullable
    public UUID getWinner() {
        return winner;
    }

    /**
     * Loser of the {@link Match}. May be null if there was a no loser!
     *
     * @return {@link UUID} of the loser of the {@link Match}. May be null if there was a no loser!
     */
    @Nullable
    public UUID getLoser() {
        return loser;
    }

    /**
     * End reason of the {@link Match}. If reason is {@link Reason#OPPONENT_DEFEAT}, {@link #getWinner()} and {@link #getLoser()} is guaranteed to not return null.
     *
     * @return {@link Reason} that is the end reason of the {@link Match}.
     */
    @Nonnull
    public Reason getReason() {
        return reason;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
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
