package me.realized.duels.api.spectate;

import java.util.UUID;
import me.realized.duels.api.arena.Arena;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Spectator spectating a match.
 *
 * @since 3.4.1
 */
public interface Spectator {

    /**
     * The {@link UUID} of this spectator.
     *
     * @return {@link UUID} of this spectator.
     */
    @NotNull
    UUID getUuid();

    /**
     * The {@link Player} instance of this spectator.
     *
     * @return The {@link Player} instance of this spectator or null if player is not online.
     * @since 3.5.1
     */
    @Nullable
    Player getPlayer();

    /**
     * The {@link UUID} of the player this spectator is spectating.
     *
     * @return {@link UUID} of the player this spectator is spectating.
     * @since 3.5.1
     */
    @NotNull
    UUID getTargetUuid();

    /**
     * The {@link Player} instance of the player this spectator is spectating.
     *
     * @return The {@link Player} instance of player this spectator is spectating or null if player is not online.
     * @since 3.5.1
     */
    @Nullable
    Player getTarget();

    /**
     * The {@link Arena} this spectator is spectating.
     *
     * @return {@link Arena} this spectator is spectating.
     */
    @NotNull
    Arena getArena();
}
