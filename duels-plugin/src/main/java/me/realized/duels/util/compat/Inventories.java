package me.realized.duels.util.compat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import me.realized.duels.util.reflect.ReflectionUtil;
import org.bukkit.inventory.Inventory;

public final class Inventories {

    private static final Field CB_INVENTORY;
    private static final Field CB_INVENTORY_TITLE;
    private static final Method CHAT_SERIALIZER_A;

    static {
        CB_INVENTORY = ReflectionUtil.getDeclaredField(ReflectionUtil.getCBClass("inventory.CraftInventory"), "inventory");
        CB_INVENTORY_TITLE = ReflectionUtil.getDeclaredField(ReflectionUtil.getCBClass("inventory.CraftInventoryCustom$MinecraftInventory"), "title");
        CHAT_SERIALIZER_A = CompatUtil.is1_13() ? ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("IChatBaseComponent$ChatSerializer"), "a", String.class) : null;
    }

    public static void setTitle(final Inventory inventory, final String title) {
        try {
            Object value = title;

            // In 1.13, title field was changed to IChatBaseComponent, but the change was reverted in 1.14.
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
