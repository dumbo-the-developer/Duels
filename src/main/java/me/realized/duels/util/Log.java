package me.realized.duels.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

public final class Log {

    public interface LogSource {

        void log(final Level level, final String s);
    }

    private static List<LogSource> sources = new ArrayList<>();

    private Log() {}

    public static void addSource(final LogSource source) {
        sources.add(source);
    }

    public static void clearSources() {
        sources.clear();
    }

    private static void log(final Level level, final String s) {
        for (final LogSource source : sources) {
            source.log(level, s);
        }
    }

    public static void info(final String s) {
        log(Level.INFO, s);
    }

    public static void error(final String s) {
        for (final LogSource source : sources) {
            if (source instanceof Plugin) {
                Bukkit.getConsoleSender().sendMessage("[" + ((Plugin) source).getName() + "] " + ChatColor.RED + s);
            } else {
                source.log(Level.SEVERE, s);
            }
        }
    }

    public static void error(final Loadable loadable, final String s) {
        error(loadable.getClass().getSimpleName() + ": " + s);
    }
}
