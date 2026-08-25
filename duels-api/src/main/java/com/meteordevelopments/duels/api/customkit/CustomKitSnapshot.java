package com.meteordevelopments.duels.api.customkit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an immutable snapshot of a custom kit at the time a duel request was sent.
 */
public interface CustomKitSnapshot {

    /**
     * Unique identifier of the original custom kit.
     *
     * @return kit ID
     */
    @NotNull
    UUID getKitId();

    /**
     * UUID of the kit owner.
     *
     * @return owner UUID
     */
    @NotNull
    UUID getOwner();

    /**
     * Name of the kit.
     *
     * @return kit name
     */
    @NotNull
    String getName();

    /**
     * Description lines of the kit.
     *
     * @return description lore lines
     */
    @NotNull
    List<String> getDescription();

    /**
     * Display icon ItemStack for GUI preview.
     *
     * @return icon item stack
     */
    @NotNull
    ItemStack getIcon();

    /**
     * Inventory layout mapping (slot index 0-35 -> defensive copy of ItemStack).
     *
     * @return unmodifiable inventory items map
     */
    @NotNull
    Map<Integer, ItemStack> getItems();

    /**
     * Armor contents mapping (0: Helmet, 1: Chestplate, 2: Leggings, 3: Boots).
     *
     * @return unmodifiable armor items map
     */
    @NotNull
    Map<Integer, ItemStack> getArmor();

    /**
     * Offhand item (or null if empty).
     *
     * @return defensive copy of offhand item
     */
    @Nullable
    ItemStack getOffHand();

    /**
     * Equips a player with deep-cloned items from this snapshot.
     *
     * @param player player to equip
     * @return true if equipped successfully
     */
    boolean equip(@NotNull Player player);
}
