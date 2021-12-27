package me.realized.duels.api.event.kit;

import java.util.Objects;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.kit.KitManager;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Kit} is created.
 *
 * @see KitManager#create(Player, String)
 */
public class KitCreateEvent extends KitEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Player source;

    public KitCreateEvent(@NotNull final Player source, @NotNull final Kit kit) {
        super(source, kit);
        Objects.requireNonNull(source, "source");
        this.source = source;
    }

    @NotNull
    @Override
    public Player getSource() {
        return source;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
