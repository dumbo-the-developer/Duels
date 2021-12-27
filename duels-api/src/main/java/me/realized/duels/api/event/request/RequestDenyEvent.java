package me.realized.duels.api.event.request;

import me.realized.duels.api.request.Request;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} denies a {@link Request} from a {@link Player}.
 *
 * @since 3.2.1
 */
public class RequestDenyEvent extends RequestEvent {

    private static final HandlerList handlers = new HandlerList();

    /**
     * @param source {@link Player} who is denying this {@link Request}.
     * @param target {@link Player} who sent this {@link Request}.
     * @param request {@link Request} that is being handled.
     */
    public RequestDenyEvent(@NotNull final Player source, @NotNull final Player target, @NotNull final Request request) {
        super(source, target, request);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
