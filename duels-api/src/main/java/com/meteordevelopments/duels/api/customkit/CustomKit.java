package com.meteordevelopments.duels.api.customkit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a player-created custom duel kit.
 */
public interface CustomKit {

    /**
     * Unique stable identifier for this custom kit.
     *
     * @return unique ID
     */
    @NotNull
    UUID getUniqueId();

    /**
     * UUID of the player who created and owns this custom kit.
     *
     * @return owner UUID
     */
    @NotNull
    UUID getOwner();

    /**
     * Name of this custom kit.
     *
     * @return kit name
     */
    @NotNull
    String getName();

    /**
     * Sets the name of this custom kit.
     *
     * @param name new kit name
     */
    void setName(@NotNull String name);

    /**
     * Description lines of this custom kit for GUI display.
     *
     * @return description lore lines
     */
    @NotNull
    List<String> getDescription();

    /**
     * Sets the description lines of this custom kit.
     *
     * @param description new description lines
     */
    void setDescription(@NotNull List<String> description);

    /**
     * Display icon ItemStack for GUI representations.
     *
     * @return icon item stack
     */
    @NotNull
    ItemStack getIcon();

    /**
     * Sets the display icon ItemStack.
     *
     * @param icon new icon item stack
     */
    void setIcon(@NotNull ItemStack icon);

    /**
     * Inventory layout mapping (slot index 0-35 -> ItemStack).
     *
     * @return inventory items map
     */
    @NotNull
    Map<Integer, ItemStack> getItems();

    /**
     * Armor contents mapping (0: Helmet, 1: Chestplate, 2: Leggings, 3: Boots).
     *
     * @return armor items map
     */
    @NotNull
    Map<Integer, ItemStack> getArmor();

    /**
     * Offhand item (or null/air if empty).
     *
     * @return offhand item
     */
    @Nullable
    ItemStack getOffHand();

    /**
     * Sets the offhand item.
     *
     * @param offHand offhand item
     */
    void setOffHand(@Nullable ItemStack offHand);

    /**
     * Creation timestamp in milliseconds.
     *
     * @return created epoch millis
     */
    long getCreated();

    /**
     * Last modified timestamp in milliseconds.
     *
     * @return modified epoch millis
     */
    long getModified();

    /**
     * Updates the last modified timestamp to current time.
     */
    void updateModified();

    /**
     * Equips the player with the contents of this custom kit.
     *
     * @param player player to equip
     * @return true if equipped successfully
     */
    boolean equip(@NotNull Player player);

    /**
     * Creates an immutable snapshot of this kit at the current moment.
     *
     * @return immutable snapshot
     */
    @NotNull
    CustomKitSnapshot toSnapshot();

    /**
     * Creates a deep clone of this custom kit.
     *
     * @return cloned custom kit
     */
    @NotNull
    CustomKit clone();
}
