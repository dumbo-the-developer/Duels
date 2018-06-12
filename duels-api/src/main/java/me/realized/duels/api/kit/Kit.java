package me.realized.duels.api.kit;

import javax.annotation.Nonnull;
import me.realized.duels.api.event.kit.KitEquipEvent;
import org.bukkit.entity.Player;

public interface Kit {

    /**
     * @return Name of the kit
     */
    @Nonnull
    String getName();


    /**
     * Calls {@link KitEquipEvent}.
     *
     * @param player Player to equip the kit items
     */
    void equip(@Nonnull final Player player);
}
