package me.realized.duels.api.event.spectate;

import java.util.Objects;
import me.realized.duels.api.event.SourcedEvent;
import me.realized.duels.api.spectate.Spectator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event caused by a {@link Spectator}.
 */
public abstract class SpectateEvent extends SourcedEvent {

    private final Player source;
    private final Spectator spectator;

    SpectateEvent(@NotNull final Player source, @NotNull Spectator spectator) {
        super(source);
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(spectator, "spectator");
        this.source = source;
        this.spectator = spectator;
    }

    @NotNull
    @Override
    public Player getSource() {
        return source;
    }

    /**
     * {@link Spectator} instance associated with this event.
     *
     * @return Never-null {@link Spectator} instance associated with this event.
     */
    @NotNull
    public Spectator getSpectator() {
        return spectator;
    }
}
