package me.realized.duels.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class KitRemoveEvent extends KitEvent {

    private static final HandlerList handlers = new HandlerList();

    public KitRemoveEvent(String name, Player player) {
        super(name, player);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
