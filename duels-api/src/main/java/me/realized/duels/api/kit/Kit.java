package me.realized.duels.api.kit;

import javax.annotation.Nonnull;
import me.realized.duels.api.event.kit.KitEquipEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Kit {

    /**
     * @return Name of the kit
     */
    @Nonnull
    String getName();


    /**
     * @return item displayed in the kit selector gui
     */
    @Nonnull
    ItemStack getDisplayed();


    /**
     * If usePermission is enabled, players must have the permission 'duels.kit.[name] (space in name replaced to dash)' to use the kit.
     *
     * @return true if usePermission is enabled, otherwise false
     */
    boolean isUsePermission();


    /**
     * @param usePermission true to enable usePermission, otherwise false
     */
    void setUsePermission(final boolean usePermission);


    /**
     * If arenaSpecific is enabled, only arenas with their name starting with this kit's name appended with an underscore will be available when playing with this kit.
     *
     * @return true if arenaSpecific is enabled, otherwise false
     */
    boolean isArenaSpecific();


    /**
     * @param arenaSpecific true to enable arenaSpecific, otherwise false
     */
    void setArenaSpecific(final boolean arenaSpecific);


    /**
     * Calls {@link KitEquipEvent}.
     *
     * @param player Player to equip the kit items
     */
    void equip(@Nonnull final Player player);
}
