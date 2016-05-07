package me.realized.duels.event;

import me.realized.duels.kits.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class KitLoadEvent extends KitEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Kit kit;

    public KitLoadEvent(String name, Kit kit, Player player) {
        super(name, player);
        this.kit = kit;
    }

    public Kit getKit() {
        return kit;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
