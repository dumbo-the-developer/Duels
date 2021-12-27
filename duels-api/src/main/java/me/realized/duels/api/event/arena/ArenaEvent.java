package me.realized.duels.api.event.arena;

import java.util.Objects;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.event.SourcedEvent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an event caused by a {@link Arena}.
 */
public abstract class ArenaEvent extends SourcedEvent {

    private final Arena arena;

    ArenaEvent(@Nullable final CommandSender source, @NotNull final Arena arena) {
        super(source);
        Objects.requireNonNull(arena, "arena");
        this.arena = arena;
    }

    /**
     * {@link Arena} instance associated with this event.
     *
     * @return Never-null {@link Arena} instance associated with this event.
     */
    @NotNull
    public Arena getArena() {
        return arena;
    }
}
