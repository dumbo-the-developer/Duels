package me.realized.duels.api.event.arena;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.realized.duels.api.arena.Arena;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

/**
 * Called when an {@link Arena} is removed.
 *
 * @see Arena#isRemoved()
 */
public class ArenaRemoveEvent extends ArenaEvent {

    private static final HandlerList handlers = new HandlerList();

    public ArenaRemoveEvent(@Nullable final CommandSender source, @Nonnull final Arena arena) {
        super(source, arena);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
