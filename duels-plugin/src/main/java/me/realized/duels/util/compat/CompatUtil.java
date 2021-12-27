package me.realized.duels.util.compat;

import me.realized.duels.util.reflect.ReflectionUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public final class CompatUtil {

    private static final boolean ATTRIBUTES, ITEM_FLAGS, SEND_TITLE;

    static {
        ATTRIBUTES = ReflectionUtil.getMethodUnsafe(ItemMeta.class, "getAttributeModifiers") != null;
        ITEM_FLAGS = ReflectionUtil.getClassUnsafe("org.bukkit.inventory.ItemFlag") != null;
        SEND_TITLE = ReflectionUtil.getMethodUnsafe(Player.class, "sendTitle", String.class, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE) != null;
    }

    public static boolean is1_13() {
        return ReflectionUtil.getMajorVersion() == 13;
    }

    public static boolean isPre1_14() {
        return ReflectionUtil.getMajorVersion() < 14;
    }

    public static boolean isPre1_13() {
        return ReflectionUtil.getMajorVersion() < 13;
    }

    public static boolean isPre1_12() {
        return ReflectionUtil.getMajorVersion() < 12;
    }

    public static boolean isPre1_10() {
        return ReflectionUtil.getMajorVersion() < 10;
    }

    public static boolean isPre1_9() {
        return ReflectionUtil.getMajorVersion() < 9;
    }

    public static boolean hasAttributes() {
        return ATTRIBUTES;
    }

    public static boolean hasItemFlag() {
        return ITEM_FLAGS;
    }

    public static boolean hasSendTitle() {
        return SEND_TITLE;
    }

    private CompatUtil() {}
}