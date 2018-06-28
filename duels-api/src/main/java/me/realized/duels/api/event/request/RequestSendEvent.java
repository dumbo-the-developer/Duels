package me.realized.duels.api.event.request;

import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.api.event.SourcedEvent;
import me.realized.duels.api.request.Request;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a player sends a Request to a player.
 */
public class RequestSendEvent extends SourcedEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Player source;
    @Getter
    private final Player target;
    @Getter
    private final Request request;
    @Getter
    @Setter
    private boolean cancelled;

    public RequestSendEvent(@Nonnull final Player source, @Nonnull Player target, @Nonnull Request request) {
        super(source);
        this.source = source;
        this.target = target;
        this.request = request;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
