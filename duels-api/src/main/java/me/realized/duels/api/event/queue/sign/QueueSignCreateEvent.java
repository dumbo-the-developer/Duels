package me.realized.duels.api.event.queue.sign;

import me.realized.duels.api.queue.sign.QueueSign;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link QueueSign} is created.
 *
 * @since 3.2.0
 */
public class QueueSignCreateEvent extends QueueSignEvent {

    private static final HandlerList handlers = new HandlerList();

    public QueueSignCreateEvent(@NotNull final Player source, @NotNull final QueueSign queueSign) {
        super(source, queueSign);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
