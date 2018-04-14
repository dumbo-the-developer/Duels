package me.realized.duels.util.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class InventoryUtil {

    private InventoryUtil() {}

    public static void fill(final Inventory inventory, final ItemStack item, final int... slots) {
        for (final int slot : slots) {
            inventory.setItem(slot, item);
        }
    }

    public static void fillRange(final Inventory inventory, final int from, final int to, final ItemStack item) {
        fillRange(inventory, from, to, item, true);
    }

    public static void fillRange(final Inventory inventory, final int from, final int to, final ItemStack item, final boolean force) {
        for (int slot = from; slot < to; slot++) {
            final ItemStack target = inventory.getItem(slot);

            if (target != null && target.getType() != Material.AIR && !force) {
                continue;
            }

            inventory.setItem(slot, item);
        }
    }

    public static void fillEmpty(final Inventory inventory, final ItemStack item) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            final ItemStack target = inventory.getItem(slot);

            if (target != null && target.getType() != Material.AIR) {
                inventory.setItem(slot, item);
            }
        }
    }

}
