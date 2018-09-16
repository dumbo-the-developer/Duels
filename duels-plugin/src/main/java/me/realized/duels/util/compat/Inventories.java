package me.realized.duels.util.compat;

import java.lang.reflect.InvocationTargetException;
import org.bukkit.inventory.Inventory;

public final class Inventories extends CompatBase {

    public static void setTitle(final Inventory inventory, final String title) {
        try {
            Object value = title;

            if (CHAT_SERIALIZER_A != null) {
                value = CHAT_SERIALIZER_A.invoke(null, "{\"text\": \"" + title + "\"}");
            }

            CB_INVENTORY_TITLE.set(CB_INVENTORY.get(inventory), value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private Inventories() {}
}
