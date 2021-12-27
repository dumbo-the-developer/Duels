package me.realized.duels.util.compat.nbt;

import java.lang.reflect.Method;
import me.realized.duels.util.reflect.ReflectionUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

/**
 * Used to store kit item identifiers in items for versions 1.8 - 1.13. For 1.14 and above, {@link PersistentDataContainer} is used instead.
 */
public final class NBT {

    private static final Method AS_NMS_COPY;
    private static final Method AS_BUKKIT_COPY;

    private static final Class<?> TAG_COMPOUND;

    private static final Method GET_TAG;
    private static final Method SET_TAG;
    private static final Method SET_STRING;
    private static final Method REMOVE;
    private static final Method HAS_KEY;

    static {
        final Class<?> CB_ITEMSTACK = ReflectionUtil.getCBClass("inventory.CraftItemStack");
        final Class<?> NMS_ITEMSTACK = ReflectionUtil.getNMSClass("ItemStack");
        AS_NMS_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asNMSCopy", ItemStack.class);
        AS_BUKKIT_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asBukkitCopy", NMS_ITEMSTACK);

        TAG_COMPOUND = ReflectionUtil.getNMSClass("NBTTagCompound");
        GET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "getTag");
        SET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "setTag", TAG_COMPOUND);
        SET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "setString", String.class, String.class);
        REMOVE = ReflectionUtil.getMethod(TAG_COMPOUND, "remove", String.class);
        HAS_KEY = ReflectionUtil.getMethod(TAG_COMPOUND, "hasKey", String.class);
    }

    public static ItemStack setItemString(final ItemStack item, final String key, final Object value) {
        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);
            Object tag = GET_TAG.invoke(nmsItem);

            if (tag == null) {
                tag = TAG_COMPOUND.newInstance();
            }

            SET_STRING.invoke(tag, key, value.toString());
            SET_TAG.invoke(nmsItem, tag);
            return (ItemStack) AS_BUKKIT_COPY.invoke(null, nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
            return item;
        }
    }

    public static ItemStack removeItemTag(final ItemStack item, final String key) {
        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);
            Object tag = GET_TAG.invoke(nmsItem);

            if (tag == null) {
                return item;
            }

            REMOVE.invoke(tag, key);
            SET_TAG.invoke(nmsItem, tag);
            return (ItemStack) AS_BUKKIT_COPY.invoke(null, nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
            return item;
        }
    }

    public static boolean hasItemKey(final ItemStack item, final String key) {
        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);

            if (nmsItem == null) {
                return false;
            }

            final Object tag = GET_TAG.invoke(nmsItem);
            return tag != null && (boolean) HAS_KEY.invoke(tag, key);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private NBT() {}
}
