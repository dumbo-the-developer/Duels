package com.meteordevelopments.duels.api.event.kit;

import com.meteordevelopments.duels.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Called when a {@link Player} equips a {@link Kit}.
 *
 * @see Kit#equip(Player)
 */
public class KitEquipEvent extends KitEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player source;
    private boolean cancelled;

    public KitEquipEvent(@NotNull final Player source, @NotNull final Kit kit) {
        super(source, kit);
        Objects.requireNonNull(source, "source");
        this.source = source;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * {@link Player} who is equipping the {@link Kit}.
     *
     * @return Never-null {@link Player} who is equipping the {@link Kit}.
     */
    @NotNull
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

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
