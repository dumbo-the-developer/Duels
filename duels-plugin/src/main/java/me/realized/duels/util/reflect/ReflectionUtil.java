package me.realized.duels.util.reflect;

import me.realized.duels.util.Log;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtil {

    private static final String CRAFTBUKKIT_PACKAGE;

    static {
        CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();
        // Add debug information
        System.out.println("Debug: CRAFTBUKKIT_PACKAGE = " + CRAFTBUKKIT_PACKAGE);
    }

    private ReflectionUtil() {
    }

    public static Class<?> getClassUnsafe(final String name) {
        try {
            System.out.println("Debug: Trying to get class " + name);
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            System.out.println("Debug: Class not found - " + name);
            return null;
        }
    }

    public static Method getMethodUnsafe(final Class<?> clazz, final String name, final Class<?>... parameters) {
        try {
            System.out.println("Debug: Trying to get method " + name + " from class " + clazz.getName());
            return clazz.getMethod(name, parameters);
        } catch (NoSuchMethodException ex) {
            System.out.println("Debug: Method not found - " + name);
            return null;
        }
    }

    public static Class<?> getNMSClass(final String name, final boolean logError) {
        try {
            String className = "net.minecraft.server." + name;
            System.out.println("Debug: Trying to get NMS class " + className);
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            if (logError) {
                Log.error("Failed to get NMS class: " + name, ex);
            }
            System.out.println("Debug: NMS class not found - " + name);
            return null;
        }
    }

    public static Class<?> getNMSClass(final String name) {
        return getNMSClass(name, true);
    }

    public static Class<?> getCBClass(final String path, final boolean logError) {
        try {
            String className = CRAFTBUKKIT_PACKAGE + "." + path;
            System.out.println("Debug: Trying to get CB class " + className);
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            if (logError) {
                Log.error("Failed to get CB class: " + path, ex);
            }
            System.out.println("Debug: CB class not found - " + path);
            return null;
        }
    }

    public static Class<?> getCBClass(final String path) {
        return getCBClass(path, true);
    }

    public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... parameters) {
        try {
            System.out.println("Debug: Trying to get method " + name + " from class " + clazz.getName());
            return clazz.getMethod(name, parameters);
        } catch (NoSuchMethodException ex) {
            Log.error("Failed to get method: " + name + " from class: " + clazz.getName(), ex);
            return null;
        }
    }

    private static Method findDeclaredMethod(final Class<?> clazz, final String name, final Class<?>... parameters) throws NoSuchMethodException {
        System.out.println("Debug: Trying to find declared method " + name + " from class " + clazz.getName());
        final Method method = clazz.getDeclaredMethod(name, parameters);
        method.setAccessible(true);
        return method;
    }

    public static Method getDeclaredMethod(final Class<?> clazz, final String name, final Class<?>... parameters) {
        try {
            return findDeclaredMethod(clazz, name, parameters);
        } catch (NoSuchMethodException ex) {
            Log.error("Failed to get declared method: " + name + " from class: " + clazz.getName(), ex);
            return null;
        }
    }

    public static Method getDeclaredMethodUnsafe(final Class<?> clazz, final String name, final Class<?>... parameters) {
        try {
            return findDeclaredMethod(clazz, name, parameters);
        } catch (NoSuchMethodException ex) {
            System.out.println("Debug: Declared method not found - " + name);
            return null;
        }
    }

    public static Field getField(final Class<?> clazz, final String name) {
        try {
            System.out.println("Debug: Trying to get field " + name + " from class " + clazz.getName());
            return clazz.getField(name);
        } catch (NoSuchFieldException ex) {
            Log.error("Failed to get field: " + name + " from class: " + clazz.getName(), ex);
            return null;
        }
    }

    public static Field getDeclaredField(final Class<?> clazz, final String name) {
        try {
            System.out.println("Debug: Trying to get declared field " + name + " from class " + clazz.getName());
            final Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ex) {
            Log.error("Failed to get declared field: " + name + " from class: " + clazz.getName(), ex);
            return null;
        }
    }

    public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... parameters) {
        try {
            System.out.println("Debug: Trying to get constructor from class " + clazz.getName());
            return clazz.getConstructor(parameters);
        } catch (NoSuchMethodException ex) {
            Log.error("Failed to get constructor from class: " + clazz.getName(), ex);
            return null;
        }
    }
}