package me.realized.duels.api.event.user;

import javax.annotation.Nonnull;
import lombok.Getter;
import me.realized.duels.api.user.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a new User is created.
 */
public class UserCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final User user;

    public UserCreateEvent(@Nonnull final User user) {
        this.user = user;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
