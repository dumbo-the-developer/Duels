package me.realized.duels.event;

import me.realized.duels.dueling.Request;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class RequestSendEvent extends RequestEvent {

    private static final HandlerList handlers = new HandlerList();

    public RequestSendEvent(Request request, Player sender, Player target) {
        super(request, sender, target);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
