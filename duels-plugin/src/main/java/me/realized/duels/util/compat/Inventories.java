package me.realized.duels.util.compat;

import java.lang.reflect.InvocationTargetException;
import org.bukkit.inventory.Inventory;

public final class Inventories extends CompatBase {

    public static String getTitle(final Inventory inventory) {
        try {
            if (CompatUtil.isPre1_14()) {
                return inventory.getTitle();
            }

            return (String) CB_INVENTORY_TITLE.get(CB_INVENTORY.get(inventory));
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            return "ERROR LOADING TITLE!";
        }
    }

    public static void setTitle(final Inventory inventory, final String title) {
        try {
            Object value = title;

            // In 1.13, title field was changed to IChatBaseComponent, but the change was reverted in 1.14.
            if (CompatUtil.is1_13() && CHAT_SERIALIZER_A != null) {
                value = CHAT_SERIALIZER_A.invoke(null, "{\"text\": \"" + title + "\"}");
            }

            CB_INVENTORY_TITLE.set(CB_INVENTORY.get(inventory), value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private Inventories() {}
}
