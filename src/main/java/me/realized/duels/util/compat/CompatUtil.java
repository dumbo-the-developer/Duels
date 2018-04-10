package me.realized.duels.util.compat;

import org.bukkit.Bukkit;

public final class CompatUtil {

    private CompatUtil() {}

    public static boolean isPre1_9() {
        return (Bukkit.getVersion().contains("1.8") || isPre1_8());
    }

    public static boolean isPre1_8() {
        return Bukkit.getVersion().contains("1.7");
    }
}
