package me.realized.duels.api.queue.sign;

import me.realized.duels.api.event.queue.sign.QueueSignRemoveEvent;
import me.realized.duels.api.queue.DQueue;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a QueueSign loaded on the server.
 */
public interface QueueSign {

    /**
     * The {@link Location} of this {@link QueueSign}.
     *
     * @return Never-null {@link Location} of this {@link QueueSign}.
     */
    @NotNull
    Location getLocation();


    /**
     * The {@link DQueue} that is linked with this {@link QueueSign}.
     *
     * @return Never-null {@link DQueue} that is linked with this {@link QueueSign}.
     */
    @NotNull
    DQueue getQueue();


    /**
     * Whether or not this {@link QueueSign} has been removed.
     *
     * @return True if this {@link QueueSign} has been removed. False otherwise.
     * @see QueueSignRemoveEvent
     */
    boolean isRemoved();
}
