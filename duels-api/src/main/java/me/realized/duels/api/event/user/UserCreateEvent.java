package me.realized.duels.api.event.user;

import java.util.Objects;
import me.realized.duels.api.user.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a new {@link User} is created.
 */
public class UserCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final User user;

    public UserCreateEvent(@NotNull final User user) {
        Objects.requireNonNull(user, "user");
        this.user = user;
    }

    /**
     * The {@link User} that was created.
     *
     * @return Never-null {@link User} that was created.
     */
    @NotNull
    public User getUser() {
        return user;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
