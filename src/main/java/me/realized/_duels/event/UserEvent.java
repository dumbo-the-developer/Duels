package me.realized._duels.event;

import me.realized._duels.data.UserData;
import org.bukkit.event.Event;

abstract class UserEvent extends Event {

    private final UserData user;

    public UserEvent(UserData user) {
        this.user = user;
    }

    public UserData getUser() {
        return user;
    }
}
