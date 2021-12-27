package me.realized.duels.api.event.queue;

import java.util.Objects;
import me.realized.duels.api.queue.DQueue;
import me.realized.duels.api.queue.DQueueManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is joining a {@link DQueue}.
 *
 * @see DQueueManager#addToQueue(Player, DQueue)
 * @since 3.2.0
 */
public class QueueJoinEvent extends QueueEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player source;
    private boolean cancelled;

    public QueueJoinEvent(@NotNull final Player source, @NotNull final DQueue queue) {
        super(source, queue);
        Objects.requireNonNull(source, "source");
        this.source = source;
    }

    /**
     * {@link Player} who is joining the {@link DQueue}.
     *
     * @return Never-null {@link Player} who is joining the {@link DQueue}.
     */
    @NotNull
    @Override
    public Player getSource() {
        return source;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
