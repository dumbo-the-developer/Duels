package me.realized.duels.api.match;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.realized.duels.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Match {

    /**
     * {@link System#currentTimeMillis()} subtracted by the result of this method will give the duration of the current match in milliseconds.
     *
     * @return start of this match in milliseconds.
     */
    long getStart();


    /**
     * @return The kit used in this match. If players are using their own inventories, this will return null
     */
    @Nullable
    Kit getKit();


    /**
     * @param player Player to get the bet items
     * @return List of items the player bet for this match.
     */
    List<ItemStack> getItems(@Nonnull final Player player);


    /**
     * @return The bet amount for this match or 0 if no bet was specified
     */
    int getBet();

}
