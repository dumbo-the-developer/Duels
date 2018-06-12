package me.realized.duels.api.event.arena;

import me.realized.duels.api.arena.Arena;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

/**
 * Called when an Arena is created.
 */
public class ArenaCreateEvent extends ArenaEvent {

    private static final HandlerList handlers = new HandlerList();

    public ArenaCreateEvent(final CommandSender source, final Arena arena) {
        super(source, arena);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
