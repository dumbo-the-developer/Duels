package me.realized.duels.api.event.arena;

import lombok.Getter;
import lombok.Setter;
import me.realized.duels.api.arena.Arena;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when an Arena is enabled or disabled.
 *
 * @see Arena#setDisabled(CommandSender, boolean)
 */
public class ArenaStateChangeEvent extends ArenaEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    @Setter
    private boolean cancelled;
    @Getter
    @Setter
    private boolean disabled;

    public ArenaStateChangeEvent(final CommandSender source, final Arena arena, final boolean disabled) {
        super(source, arena);
        this.disabled = disabled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
