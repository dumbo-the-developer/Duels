package me.realized.duels.util.inventory;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public final class InventoryUtil {

    private InventoryUtil() {}

    public static Inventory deepCopyOf(final Inventory other) {
        final Inventory result = Bukkit.createInventory(null, other.getSize(), other.getTitle());

        for (int i = 0; i < other.getSize(); i++) {
            result.setItem(i, other.getItem(i));
        }

        return result;
    }
}
