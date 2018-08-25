package me.realized.duels.api.event.kit;

import java.util.Objects;
import javax.annotation.Nonnull;
import me.realized.duels.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a {@link Player} equips a {@link Kit}.
 *
 * @see Kit#equip(Player)
 */
public class KitEquipEvent extends KitEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player source;
    private boolean cancelled;

    public KitEquipEvent(@Nonnull final Player source, @Nonnull final Kit kit) {
        super(source, kit);
        Objects.requireNonNull(source, "source");
        this.source = source;
    }

    /**
     * {@link Player} who is equipping the {@link Kit}.
     *
     * @return Never-null {@link Player} who is equipping the {@link Kit}.
     */
    @Nonnull
    @Override
    public Player getSource() {
        return source;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
