package me.realized.duels.event;

import me.realized.duels.data.UserData;
import org.bukkit.event.HandlerList;

public class UserCreateEvent extends UserEvent {

    private static final HandlerList handlers = new HandlerList();

    public UserCreateEvent(UserData user) {
        super(user);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
