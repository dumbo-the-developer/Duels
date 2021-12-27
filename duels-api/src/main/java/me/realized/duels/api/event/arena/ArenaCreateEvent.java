package me.realized.duels.api.event.arena;

import me.realized.duels.api.arena.Arena;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a new {@link Arena} is created.
 */
public class ArenaCreateEvent extends ArenaEvent {

    private static final HandlerList handlers = new HandlerList();

    public ArenaCreateEvent(@Nullable final CommandSender source, @NotNull final Arena arena) {
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
