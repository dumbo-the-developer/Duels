package me.realized.duels.util.reflect;

import me.realized.duels.util.Log;
import me.realized.duels.util.NumberUtil;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtil {

    private static final String PACKAGE_VERSION;
    private static final Long MAJOR_VERSION;

    static {
    	final String packageName = Bukkit.getServer().getBukkitVersion().split(".")[1];
        PACKAGE_VERSION = packageName.substring(packageName.lastIndexOf('.') + 1);
        MAJOR_VERSION = NumberUtil.parseLong(packageName).orElse(0);
    }

    public static Long getMajorVersion() {
        return MAJOR_VERSION;
    }
    private ReflectionUtil() {
    }


    public static Class<?> getClassUnsafe(final String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static Method getMethodUnsafe(final Class<?> clazz, final String name, final Class<?>... parameters) {
        try {
            return clazz.getMethod(name, parameters);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static Class<?> getNMSClass(final String name, final boolean logError) {
        try {
            return Class.forName("net.minecraft" + (getMajorVersion() < 17 ? (".server." + PACKAGE_VERSION) : "") + "." + name);
        } catch (ClassNotFoundException ex) {
            if (logError) {
                Log.error(ex.getMessage(), ex);
            }

            return null;
        }
    }

    public static Class<?> getNMSClass(final String name) {
        return getNMSClass(name, true);
    }

    public static Class<?> getCBClass(final String path, final boolean logError) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + PACKAGE_VERSION + "." + path);
        } catch (ClassNotFoundException ex) {
            if (logError) {
                Log.error(ex.getMessage(), ex);
            }

            return null;
        }
    }

    public static Class<?> getCBClass(final String path) {
        return getCBClass(path, true);
    }

    public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... parameters) {
        try {
            return clazz.getMethod(name, parameters);
        } catch (NoSuchMethodException ex) {
            Log.error(ex.getMessage(), ex);
            return null;
        }
    }

    private static Method findDeclaredMethod(final Class<?> clazz, final String name, final Class<?>... parameters) throws NoSuchMethodException {
        final Method method = clazz.getDeclaredMethod(name, parameters);
        method.setAccessible(true);
        return method;
    }

    public static Method getDeclaredMethod(final Class<?> clazz, final String name, final Class<?>... parameters) {
        try {
            return findDeclaredMethod(clazz, name, parameters);
        } catch (NoSuchMethodException ex) {
            Log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public static Method getDeclaredMethodUnsafe(final Class<?> clazz, final String name, final Class<?>... parameters) {
        try {
            return findDeclaredMethod(clazz, name, parameters);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static Field getField(final Class<?> clazz, final String name) {
        try {
            return clazz.getField(name);
        } catch (NoSuchFieldException ex) {
            Log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public static Field getDeclaredField(final Class<?> clazz, final String name) {
        try {
            final Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ex) {
            Log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... parameters) {
        try {
            return clazz.getConstructor(parameters);
        } catch (NoSuchMethodException ex) {
            Log.error(ex.getMessage(), ex);
            return null;
        }
    }
}
