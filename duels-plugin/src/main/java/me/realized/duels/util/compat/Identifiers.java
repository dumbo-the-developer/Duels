package me.realized.duels.util.compat;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.compat.nbt.NBT;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class Identifiers {

    private static transient final String DUELS_ITEM_IDENTIFIER = "DuelsKitContent";

    public static ItemStack addIdentifier(final ItemStack item) {
        if (CompatUtil.isPre1_14()) {
            return NBT.setItemString(item, DUELS_ITEM_IDENTIFIER, true);
        }

        final NamespacedKey key = new NamespacedKey(DuelsPlugin.getInstance(), DUELS_ITEM_IDENTIFIER);
        final ItemMeta meta = item.getItemMeta();


        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean hasIdentifier(final ItemStack item) {
        if (CompatUtil.isPre1_14()) {
            return NBT.hasItemKey(item, DUELS_ITEM_IDENTIFIER);
        }

        final NamespacedKey key = new NamespacedKey(DuelsPlugin.getInstance(), DUELS_ITEM_IDENTIFIER);
        final ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public static ItemStack removeIdentifier(final ItemStack item) {
        if (CompatUtil.isPre1_14()) {
            return NBT.removeItemTag(item, DUELS_ITEM_IDENTIFIER);
        }

        final NamespacedKey key = new NamespacedKey(DuelsPlugin.getInstance(), DUELS_ITEM_IDENTIFIER);
        final ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().remove(key);
        item.setItemMeta(meta);
        return item;
    }

    private Identifiers() {}
}
