package me.realized.duels.utilities.compat;

import me.realized.duels.Core;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
