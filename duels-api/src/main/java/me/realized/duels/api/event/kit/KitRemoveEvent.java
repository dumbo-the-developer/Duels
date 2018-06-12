package me.realized.duels.api.event.kit;

import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.kit.KitManager;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

/**
 * Called when a Kit is created.
 *
 * @see KitManager#remove(CommandSender, String)
 */
public class KitRemoveEvent extends KitEvent {

    private static final HandlerList handlers = new HandlerList();

    public KitRemoveEvent(final CommandSender source, final Kit kit) {
        super(source, kit);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
