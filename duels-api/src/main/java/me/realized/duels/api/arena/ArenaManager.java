package me.realized.duels.api.arena;

import java.util.List;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the ArenaManager singleton used by Duels.
 */
public interface ArenaManager {

    /**
     * Attempts to find an {@link Arena} instance associated with the given name.
     *
     * @param name Name to search through the loaded arenas. Should not be null!
     * @return {@link Arena} instance that has a name matching with the given name or null if not exists.
     */
    @Nullable
    Arena get(@NotNull final String name);


    /**
     * Attempts to find an {@link Arena} instance that contains the player.
     *
     * @param player Player to search through the loaded arenas. Should not be null!
     * @return {@link Arena} instance that contains the player or null if not exists.
     */
    @Nullable
    Arena get(@NotNull final Player player);


    /**
     * Whether or not the given player is in a match. If returned true, {@link #get(Player)} is guaranteed to return a {@link Arena} instance.
     *
     * @param player Player to check if in match. Should not be null!
     * @return True if player was in a match. False otherwise.
     */
    boolean isInMatch(@NotNull final Player player);


    /**
     * An UnmodifiableList of {@link Arena}s that are currently loaded.
     *
     * @return Never-null UnmodifiableList of {@link Arena}s that are currently loaded.
     * @since 3.4.0
     */
    @NotNull
    List<Arena> getArenas();
}
