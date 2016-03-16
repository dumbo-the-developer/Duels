package me.realized.duels.event;

import me.realized.duels.kits.KitContents;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class KitLoadEvent extends KitEvent {

    private static final HandlerList handlers = new HandlerList();
    private final KitContents contents;

    public KitLoadEvent(String name, KitContents contents, Player player) {
        super(name, player);
        this.contents = contents;
    }

    public KitContents getContents() {
        return contents;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
