package me.realized.duels.api.event.match;

import me.realized.duels.api.match.Match;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Called when a {@link Match} has started.
 */
public class MatchStartEvent extends MatchEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Player[] players;

    public MatchStartEvent(@NotNull final Match match, @NotNull final Player... players) {
        super(match);
        Objects.requireNonNull(players, "players");
        this.players = players;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * The starters of the {@link Match}.
     *
     * @return Never-null {@link Player} array representing the starters of the {@link Match}.
     */
    @NotNull
    public Player[] getPlayers() {
        return players;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
