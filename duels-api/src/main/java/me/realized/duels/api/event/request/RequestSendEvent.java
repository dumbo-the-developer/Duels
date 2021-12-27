package me.realized.duels.api.event.request;

import me.realized.duels.api.request.Request;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} sends a {@link Request} to a {@link Player}.
 */
public class RequestSendEvent extends RequestEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    public RequestSendEvent(@NotNull final Player source, @NotNull final Player target, @NotNull final Request request) {
        super(source, target, request);
    }

    /**
     * Whether or not this event has been cancelled.
     *
     * @return True if this event has been cancelled. False otherwise.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Whether or not to cancel this event.
     *
     * @param cancelled True to cancel this event.
     */
    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
