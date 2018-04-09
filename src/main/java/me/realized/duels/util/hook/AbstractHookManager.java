/*
 *
 *   This file is part of TokenManager, licensed under the MIT License.
 *
 *   Copyright (c) Realized
 *   Copyright (c) contributors
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 *
 */

package me.realized.duels.util.hook;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import me.realized.duels.util.Loadable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractHookManager<P extends JavaPlugin> implements Loadable {

    protected final P plugin;
    private final Map<Class<? extends PluginHook<P>>, PluginHook<P>> hooks = new HashMap<>();

    public AbstractHookManager(final P plugin) {
        this.plugin = plugin;
    }

    protected boolean register(final String name, final Class<? extends PluginHook<P>> clazz) {
        final Plugin target = Bukkit.getPluginManager().getPlugin(name);

        if (target == null || !target.isEnabled()) {
            return false;
        }

        try {
            final Constructor<? extends PluginHook<P>> constructor = clazz.getConstructor(plugin.getClass());
            final boolean result;

            if (result = constructor != null && hooks.putIfAbsent(clazz, constructor.newInstance(plugin)) == null) {
                plugin.getLogger().info("Successfully hooked into '" + name + "'!");
            }

            return result;
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            plugin.getLogger().warning("Failed to hook into " + name + ": " + ex.getMessage());
        }

        return false;
    }

    public <T extends PluginHook<P>> T getHook(Class<T> clazz) {
        return clazz != null ? clazz.cast(hooks.get(clazz)) : null;
    }
}
