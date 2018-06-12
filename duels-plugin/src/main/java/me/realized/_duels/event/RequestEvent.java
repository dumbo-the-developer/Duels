package me.realized._duels.event;

import me.realized._duels.dueling.Request;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

abstract class RequestEvent extends Event {

    private final Request request;
    private final Player sender;
    private final Player target;

    RequestEvent(Request request, Player sender, Player target) {
        this.request = request;
        this.sender = sender;
        this.target = target;
    }

    public Request getRequest() {
        return request;
    }

    public Player getSender() {
        return sender;
    }

    public Player getTarget() {
        return target;
    }
}
