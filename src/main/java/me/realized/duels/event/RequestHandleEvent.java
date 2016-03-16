package me.realized.duels.event;

import me.realized.duels.dueling.Request;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class RequestHandleEvent extends RequestEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Action action;

    public RequestHandleEvent(Request request, Player sender, Player target, Action action) {
        super(request, sender, target);
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum Action {

        ACCEPT, DENY
    }
}
