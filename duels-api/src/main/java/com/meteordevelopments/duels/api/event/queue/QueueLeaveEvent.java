package com.meteordevelopments.duels.api.event.queue;

import com.meteordevelopments.duels.api.queue.DQueue;
import com.meteordevelopments.duels.api.queue.DQueueManager;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Called when a player is leaving a {@link DQueue}.
 *
 * @see DQueueManager#removeFromQueue(Player)
 * @since 3.2.0
 */
public class QueueLeaveEvent extends QueueEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Player source;

    public QueueLeaveEvent(@NotNull final Player source, @NotNull final DQueue queue) {
        super(source, queue);
        Objects.requireNonNull(source, "source");
        this.source = source;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * {@link Player} who is leaving the {@link DQueue}.
     *
     * @return Never-null {@link Player} who is leaving the {@link DQueue}.
     */
    @NotNull
    @Override
    public Player getSource() {
        return source;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
