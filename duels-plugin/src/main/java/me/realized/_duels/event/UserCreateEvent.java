package me.realized._duels.event;

import me.realized._duels.data.UserData;
import org.bukkit.event.HandlerList;

public class UserCreateEvent extends UserEvent {

    private static final HandlerList handlers = new HandlerList();

    public UserCreateEvent(UserData user) {
        super(user);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
