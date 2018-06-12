package me.realized.duels.api.event.kit;

import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a player equips a Kit.
 *
 * @see Kit#equip(Player)
 */
public class KitEquipEvent extends KitEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Player source;
    @Getter
    @Setter
    private boolean cancelled;

    public KitEquipEvent(@Nonnull final Player source, final Kit kit) {
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
