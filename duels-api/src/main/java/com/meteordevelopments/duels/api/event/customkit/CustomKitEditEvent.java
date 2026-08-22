package com.meteordevelopments.duels.api.event.customkit;

import com.meteordevelopments.duels.api.customkit.CustomKit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Called when a player modifies and saves a custom kit.
 */
public class CustomKitEditEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CustomKit kit;
    private boolean cancelled;

    public CustomKitEditEvent(@NotNull final Player player, @NotNull final CustomKit kit) {
        super(Objects.requireNonNull(player, "player"));
        this.kit = Objects.requireNonNull(kit, "kit");
    }

    @NotNull
    public CustomKit getKit() {
        return kit;
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
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
