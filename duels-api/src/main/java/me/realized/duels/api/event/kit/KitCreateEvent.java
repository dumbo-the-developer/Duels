package me.realized.duels.api.event.kit;

import javax.annotation.Nonnull;
import lombok.Getter;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.kit.KitManager;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Called when a Kit is created.
 *
 * @see KitManager#create(Player, String)
 */
public class KitCreateEvent extends KitEvent {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Player source;

    public KitCreateEvent(@Nonnull final Player source, final Kit kit) {
        super(source, kit);
        this.source = source;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
