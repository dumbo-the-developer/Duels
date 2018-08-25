package me.realized.duels.api.event.request;

import java.util.Objects;
import javax.annotation.Nonnull;
import me.realized.duels.api.event.SourcedEvent;
import me.realized.duels.api.request.Request;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a {@link Player} sends a {@link Request} to a {@link Player}.
 */
public class RequestSendEvent extends SourcedEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player source;
    private final Player target;
    private final Request request;

    private boolean cancelled;

    public RequestSendEvent(@Nonnull final Player source, @Nonnull final Player target, @Nonnull final Request request) {
        super(source);
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(request, "request");
        this.source = source;
        this.target = target;
        this.request = request;
    }

    /**
     * {@link Player} who is sending the {@link Request}.
     *
     * @return Never-null {@link Player} who is sending the {@link Request}.
     */
    @Nonnull
    @Override
    public Player getSource() {
        return source;
    }

    /**
     * {@link Player} who is receiving the {@link Request}.
     *
     * @return Never-null {@link Player} who is receiving the {@link Request}.
     */
    @Nonnull
    public Player getTarget() {
        return target;
    }

    /**
     * The {@link Request} that will be sent.
     *
     * @return Never-null {@link Request} that will be sent.
     */
    @Nonnull
    public Request getRequest() {
        return request;
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
