package me.realized.duels.api.event.kit;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.realized.duels.api.event.SourcedEvent;
import me.realized.duels.api.kit.Kit;
import org.bukkit.command.CommandSender;

/**
 * Represents an event caused by a {@link Kit}.
 */
public abstract class KitEvent extends SourcedEvent {

    private final Kit kit;

    KitEvent(@Nullable final CommandSender source, @Nonnull final Kit kit) {
        super(source);
        Objects.requireNonNull(kit, "kit");
        this.kit = kit;
    }

    /**
     * {@link Kit} instance associated with this event.
     *
     * @return Never-null {@link Kit} instance associated with this event.
     */
    @Nonnull
    public Kit getKit() {
        return kit;
    }
}
