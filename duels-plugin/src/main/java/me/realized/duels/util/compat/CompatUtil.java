package me.realized.duels.util.compat;

import me.realized.duels.util.NumberUtil;
import org.bukkit.Bukkit;

public final class CompatUtil {

    private static final int SUB_VERSION;
    private static final boolean ITEM_FLAGS;

    static {
        ITEM_FLAGS = ReflectionUtil.getClassUnsafe("org.bukkit.inventory.ItemFlag") != null;
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        final String[] versionInfo = packageName.substring(packageName.lastIndexOf('.') + 1).split("_");
        SUB_VERSION = NumberUtil.parseInt(versionInfo[1]).orElse(0);
    }

    private CompatUtil() {}

    public static boolean hasItemFlag() {
        return ITEM_FLAGS;
    }

    public static boolean isPre1_13() {
        return SUB_VERSION < 13;
    }

    public static boolean isPre1_10() {
        return SUB_VERSION < 10;
    }

    public static boolean isPre1_9() {
        return SUB_VERSION < 9;
    }

    public static boolean isPre1_8() {
        return SUB_VERSION < 8;
    }
}
