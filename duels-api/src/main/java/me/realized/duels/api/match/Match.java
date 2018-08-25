package me.realized.duels.api.match;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an ongoing Match.
 */
public interface Match {


    /**
     * The {@link Arena} this {@link Match} is taking place in.
     *
     * @return {@link Arena} this {@link Match} is taking place in.
     */
    @Nonnull
    Arena getArena();


    /**
     * The start of this match {@link Match} milliseconds.
     * Note: {@link System#currentTimeMillis()} subtracted by the result of this method will give the duration of the current {@link Match} in milliseconds.
     *
     * @return start of this match in milliseconds.
     */
    long getStart();


    /**
     * The {@link Kit} used in this {@link Match}.
     *
     * @return {@link Kit} used in this {@link Match} or null if players are using their own inventories.
     */
    @Nullable
    Kit getKit();


    /**
     * UnmodifiableList of {@link ItemStack}s {@link Player} has bet for this {@link Match}.
     *
     * @param player {@link Player} to get the bet items.
     * @return Never-null UnmodifiableList of {@link ItemStack}s {@link Player} has bet for this {@link Match}.
     */
    @Nonnull
    List<ItemStack> getItems(@Nonnull final Player player);


    /**
     * The bet amount for this {@link Match}.
     *
     * @return bet Bet amount for this {@link Match} or 0 if no bet was specified.
     */
    int getBet();


    /**
     * UnmodifiableSet of alive {@link Player}s in this {@link Match}.
     *
     * @return Never-null UnmodifiableSet of alive {@link Player}s in this {@link Match}.
     * @since 3.1.0
     */
    @Nonnull
    Set<Player> getPlayers();

}
