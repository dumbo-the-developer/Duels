package com.meteordevelopments.duels.api.customkit;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Manages player-created custom duel kits.
 */
public interface CustomKitManager {

    /**
     * Gets all custom kits owned by the given player UUID.
     *
     * @param owner owner UUID
     * @return unmodifiable list of custom kits
     */
    @NotNull
    List<CustomKit> getKits(@NotNull UUID owner);

    /**
     * Gets all custom kits owned by the player.
     *
     * @param player player
     * @return unmodifiable list of custom kits
     */
    @NotNull
    default List<CustomKit> getKits(@NotNull Player player) {
        return getKits(player.getUniqueId());
    }

    /**
     * Gets a custom kit by owner UUID and kit ID.
     *
     * @param owner owner UUID
     * @param id    kit ID
     * @return custom kit or null if not found
     */
    @Nullable
    CustomKit getKit(@NotNull UUID owner, @NotNull UUID id);

    /**
     * Gets a custom kit by owner UUID and kit name (case-insensitive).
     *
     * @param owner owner UUID
     * @param name  kit name
     * @return custom kit or null if not found
     */
    @Nullable
    CustomKit getKit(@NotNull UUID owner, @NotNull String name);

    /**
     * Creates and saves a new custom kit for the player.
     *
     * @param owner player who creates the kit
     * @param name  name of the kit
     * @return newly created kit or null if limit reached or name is invalid
     */
    @Nullable
    CustomKit createKit(@NotNull Player owner, @NotNull String name);

    /**
     * Saves changes made to a custom kit asynchronously.
     *
     * @param kit kit to save
     */
    void saveKit(@NotNull CustomKit kit);

    /**
     * Deletes a custom kit owned by the player.
     *
     * @param owner owner UUID
     * @param id    kit ID
     * @return deleted kit or null if not found
     */
    @Nullable
    CustomKit deleteKit(@NotNull UUID owner, @NotNull UUID id);

    /**
     * Duplicates an existing custom kit for a player under a new name.
     *
     * @param owner   player who owns the kit
     * @param kitId   ID of the kit to duplicate
     * @param newName name for the duplicated kit
     * @return duplicated kit or null if limit reached or name invalid
     */
    @Nullable
    CustomKit duplicateKit(@NotNull Player owner, @NotNull UUID kitId, @NotNull String newName);

    /**
     * Calculates the maximum custom kit limit allowed for the player based on permissions and config.
     *
     * @param player player
     * @return max kit limit (or Integer.MAX_VALUE if unlimited)
     */
    int getMaxKits(@NotNull Player player);

    /**
     * Checks if a player has reached their custom kit limit.
     *
     * @param player player
     * @return true if limit reached
     */
    boolean hasReachedLimit(@NotNull Player player);
}
