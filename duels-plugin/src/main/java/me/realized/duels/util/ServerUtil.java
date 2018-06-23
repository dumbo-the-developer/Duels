package me.realized.duels.util;

public final class ServerUtil {

    private static boolean USING_SPIGOT;

    static {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            USING_SPIGOT = true;
        } catch (ClassNotFoundException ignored) {}
    }

    private ServerUtil() {}

    public static boolean isUsingSpigot() {
        return USING_SPIGOT;
    }
}
