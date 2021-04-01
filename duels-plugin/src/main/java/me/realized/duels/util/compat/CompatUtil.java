package me.realized.duels.util.compat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public final class CompatUtil {

    private static final int SUB_VERSION;
    private static final boolean ITEM_FLAGS, SEND_TITLE, ENCHANTMENT_ID;

    private static Method GET_ENCHANTMENT_ID;

    static {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        final String[] versionInfo = packageName.substring(packageName.lastIndexOf('.') + 1).split("_");
        SUB_VERSION = NumberUtil.parseInt(versionInfo[1]).orElse(0);
        ITEM_FLAGS = ReflectionUtil.getClassUnsafe("org.bukkit.inventory.ItemFlag") != null;
        SEND_TITLE = ReflectionUtil.getMethodUnsafe(Player.class, "sendTitle", String.class, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE) != null;
        ENCHANTMENT_ID = (GET_ENCHANTMENT_ID = ReflectionUtil.getMethodUnsafe(Enchantment.class, "getId")) != null;
    }

    private CompatUtil() {}

    public static boolean hasItemFlag() {
        return ITEM_FLAGS;
    }

    public static boolean hasSendTitle() {
        return SEND_TITLE;
    }

    public static boolean hasEnchantmentId() {
        return ENCHANTMENT_ID;
    }

    public static int getEnchantmentId(final Enchantment enchantment) {
        try {
            return (int) GET_ENCHANTMENT_ID.invoke(enchantment);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public static boolean is1_13() {
        return SUB_VERSION == 13;
    }

    public static boolean isPre1_15() {
        return SUB_VERSION < 15;
    }

    public static boolean isPre1_14() {
        return SUB_VERSION < 14;
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
}
