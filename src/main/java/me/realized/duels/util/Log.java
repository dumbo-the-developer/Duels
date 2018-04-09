package me.realized.duels.util;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class Log {

    private static JavaPlugin source;

    private Log() {}

    public static void setSource(final JavaPlugin plugin) {
        source = plugin;
    }

    private static void log(final Level level, final String s) {
        if (source != null) {
            source.getLogger().log(level, s);
        }
    }

    public static void info(final String s) {
        log(Level.INFO, s);
    }

    public static void error(final String s) {
        if (source != null) {
            Bukkit.getConsoleSender().sendMessage("[" + source.getName() + "] " + ChatColor.RED + s);
        }
    }

    public static void error(final Loadable loadable, final String s) {
        error(loadable.getClass().getSimpleName() + ": " + s);
    }
}
