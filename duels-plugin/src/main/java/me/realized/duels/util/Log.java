/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.realized.duels.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

public final class Log {

    private static final List<LogSource> sources = new ArrayList<>();

    private Log() {}

    public static void addSource(final LogSource source) {
        sources.add(source);
    }

    public static void clearSources() {
        sources.clear();
    }

    public static void info(final String s) {
        for (final LogSource source : sources) {
            source.log(Level.INFO, s);
        }
    }

    public static void info(final Loadable loadable, final String s) {
        for (final LogSource source : sources) {
            source.log(Level.INFO, loadable.getClass().getSimpleName() + ": " + s);
        }
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

    public interface LogSource {

        void log(final Level level, final String s);
    }
}
