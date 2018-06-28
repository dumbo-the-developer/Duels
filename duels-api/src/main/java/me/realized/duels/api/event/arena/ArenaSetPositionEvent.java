package me.realized.duels.api.event.arena;

import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.api.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a position for an Arena is set.
 *
 * @see Arena#setPosition(Player, int, Location)
 */
public class ArenaSetPositionEvent extends ArenaEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Player source;
    @Getter
    @Setter
    private int pos;
    @Getter
    @Setter
    private Location location;
    @Getter
    @Setter
    private boolean cancelled;

    public ArenaSetPositionEvent(final Player source, final Arena arena, final int pos, @Nonnull final Location location) {
        super(source, arena);
        this.source = source;
        this.pos = pos;
        this.location = location;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
