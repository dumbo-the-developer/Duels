package me.realized.duels.util.compat;

import me.realized.duels.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class CompatUtil {

    private static final int SUB_VERSION;
    private static final boolean ITEM_FLAGS, SEND_TITLE, SET_COLLIDABLE;

    static {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        final String[] versionInfo = packageName.substring(packageName.lastIndexOf('.') + 1).split("_");
        SUB_VERSION = NumberUtil.parseInt(versionInfo[1]).orElse(0);
        ITEM_FLAGS = ReflectionUtil.getClassUnsafe("org.bukkit.inventory.ItemFlag") != null;
        SEND_TITLE = ReflectionUtil.getMethodUnsafe(Player.class, "sendTitle", String.class, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE) != null;
        SET_COLLIDABLE = ReflectionUtil.getMethodUnsafe(Player.class, "setCollidable", Boolean.TYPE) != null;
    }

    private CompatUtil() {}

    public static boolean hasItemFlag() {
        return ITEM_FLAGS;
    }

    public static boolean hasSendTitle() {
        return SEND_TITLE;
    }

    public static boolean hasSetCollidable() {
        return SET_COLLIDABLE;
    }

    public static boolean is1_13() {
        return SUB_VERSION == 13;
    }

    public static boolean isPre1_14() {
        return SUB_VERSION < 14;
    }

    public static boolean isPre1_13() {
        return SUB_VERSION < 13;
    }

    public static boolean isPre1_12() {
        return SUB_VERSION < 12;
    }

    public static boolean isPre1_9() {
        return SUB_VERSION < 9;
    }

    public static boolean isPre1_8() {
        return SUB_VERSION < 8;
    }
}
