package me.realized.duels.api.arena;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.entity.Player;

public interface ArenaManager {

    /**
     * @param name Name to search through the list of arenas
     * @return Arena instance if an arena with the given name exists, otherwise null
     */
    @Nullable
    Arena get(@Nonnull final String name);


    /**
     * @param player Player to search through the list of arenas
     * @return Arena instance if an arena containing the player exists, otherwise null
     */
    @Nullable
    Arena get(@Nonnull final Player player);


    /**
     * @param player Player to check if in match
     * @return true if in match, otherwise false. If true, {@link #get(Player)} is guaranteed to return an Arena instance.
     */
    boolean isInMatch(@Nonnull final Player player);
}
