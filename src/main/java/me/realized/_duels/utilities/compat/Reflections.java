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

package me.realized._duels.utilities.compat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import me.realized._duels.Core;
import org.bukkit.Bukkit;

public class Reflections {

    private static Map<String, Class<?>> CLASS_CACHE = new HashMap<>();
    private static Map<Class<?>, Map<String, Method>> METHOD_CACHE = new HashMap<>();

    private static String CRAFTBUKKIT_PKG;
    private static String NMS_PKG;

    static {
        final String version = Bukkit.getServer().getClass().getName().split("\\.")[3];
        NMS_PKG = "net.minecraft.server." + version + ".";
        CRAFTBUKKIT_PKG = "org.bukkit.craftbukkit." + version + ".";
    }

    public static Class<?> getNMSClass(String path) {
        return getClass(path, NMS_PKG);
    }

    public static Class<?> getCBClass(String path) {
        return getClass(path, CRAFTBUKKIT_PKG);
    }

    private static Class<?> getClass(String path, String type) {
        Class<?> cached = CLASS_CACHE.get(path);

        if (cached != null) {
            return cached;
        }

        try {
            Class<?> result = Class.forName(type + path);
            CLASS_CACHE.put(path, result);
            METHOD_CACHE.put(result, new HashMap<String, Method>());
            return result;
        } catch (ClassNotFoundException e) {
            Core.getInstance().warn("CLASS NOT FOUND: " + e.getMessage());
            return null;
        }
    }

    public static Method getMethod(Class<?> _class, String name, Class<?>... parameters) {
        Map<String, Method> methods = METHOD_CACHE.get(_class);

        if (methods == null) {
            return null;
        }

        Method cached = methods.get(name);

        if (cached != null) {
            return cached;
        }

        try {
            Method result = _class.getMethod(name, parameters);
            METHOD_CACHE.get(_class).put(name, result);
            return result;
        } catch (NoSuchMethodException e) {
            Core.getInstance().warn("METHOD NOT FOUND: " + e.getMessage());
            return null;
        }
    }
}
