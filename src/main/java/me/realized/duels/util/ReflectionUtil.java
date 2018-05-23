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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;

public final class ReflectionUtil {

    private final static String VERSION;

    static {
        VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];
    }

    private ReflectionUtil() {}

    public static Class<?> getNMSClass(final String name) {
        try {
            return Class.forName("net.minecraft.server." + VERSION + "." + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Class<?> getCBClass(final String path) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + VERSION + "." + path);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... parameters) {
        try {
            return clazz.getMethod(name, parameters);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Field getField(final Class<?> clazz, final String name) {
        try {
            return clazz.getField(name);
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
