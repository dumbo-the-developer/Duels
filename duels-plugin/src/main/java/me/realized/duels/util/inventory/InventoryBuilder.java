package me.realized.duels.util.inventory;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class InventoryBuilder {

    private final Inventory inventory;

    private InventoryBuilder(final String title, final int size) {
        this.inventory = Bukkit.createInventory(null, size, title);
    }

    public static InventoryBuilder of(final String title, final int size) {
        return new InventoryBuilder(title, size);
    }

    public InventoryBuilder set(final int slot, final ItemStack item) {
        inventory.setItem(slot, item);
        return this;
    }

    public InventoryBuilder fillRange(final int from, final int to, final ItemStack item) {
        Slots.run(from, to, slot -> inventory.setItem(slot, item));
        return this;
    }

    public Inventory build() {
        return inventory;
    }
}
