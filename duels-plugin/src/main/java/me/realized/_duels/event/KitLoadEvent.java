package me.realized._duels.event;

import me.realized._duels.kits.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class KitLoadEvent extends KitEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Kit kit;

    public KitLoadEvent(String name, Kit kit, Player player) {
        super(name, player);
        this.kit = kit;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Kit getKit() {
        return kit;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
