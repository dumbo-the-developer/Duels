package com.meteordevelopments.duels.api.event.kit;

import com.meteordevelopments.duels.api.event.SourcedEvent;
import com.meteordevelopments.duels.api.kit.Kit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents an event caused by a {@link Kit}.
 */
public abstract class KitEvent extends SourcedEvent {

    private final Kit kit;

    KitEvent(@Nullable final CommandSender source, @NotNull final Kit kit) {
        super(source);
        Objects.requireNonNull(kit, "kit");
        this.kit = kit;
    }

    /**
     * {@link Kit} instance associated with this event.
     *
     * @return Never-null {@link Kit} instance associated with this event.
     */
    @NotNull
    public Kit getKit() {
        return kit;
    }
}
