package me.realized.duels.util.compat;

import org.bukkit.inventory.ItemStack;

/**
 * Prevents players from keeping kit contents by removing items with the given key on click/interact/pickup
 */
public final class Tags extends CompatBase {

    private Tags() {}

    public static ItemStack setKey(final ItemStack item, final String key) {
        if (item == null) {
            return null;
        }

        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);
            Object tag = GET_TAG.invoke(nmsItem);

            if (tag == null) {
                tag = TAG_COMPOUND.newInstance();
            }

            SET_STRING.invoke(tag, key, "true");
            SET_TAG.invoke(nmsItem, tag);
            return (ItemStack) AS_BUKKIT_COPY.invoke(null, nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
            return item;
        }
    }

    public static boolean hasKey(final ItemStack item, final String key) {
        if (item == null) {
            return false;
        }

        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);

            if (nmsItem == null) {
                return false;
            }

            final Object tag = GET_TAG.invoke(nmsItem);
            return tag != null && GET_STRING.invoke(tag, key).equals("true");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
