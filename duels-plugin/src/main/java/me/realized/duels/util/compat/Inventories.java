package me.realized.duels.util.compat;

import org.bukkit.inventory.Inventory;

public final class Inventories extends CompatBase {

    public static void setTitle(final Inventory inventory, final String title) {
        try {
            CB_INVENTORY_TITLE.set(CB_INVENTORY.get(inventory), title);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    private Inventories() {}
}
