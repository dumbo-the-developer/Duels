package me.realized.duels.api.event.queue;

import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.queue.DQueue;
import me.realized.duels.api.queue.DQueueManager;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a {@link DQueue} is removed.
 *
 * @see DQueueManager#remove(CommandSender, Kit, int)
 * @see DQueue#isRemoved()
 * @since 3.2.0
 */
public class QueueRemoveEvent extends QueueEvent {

    private static final HandlerList handlers = new HandlerList();

    public QueueRemoveEvent(@Nullable final CommandSender source, @NotNull final DQueue queue) {
        super(source, queue);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
