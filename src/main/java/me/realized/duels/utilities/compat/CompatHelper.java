package me.realized.duels.utilities.compat;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class CompatHelper {

    private static Class<?> nmsItemStack;
    private static Method asNMSCopy;
    private static Method asBukkitCopy;
    private static Class<?> nbtTagCompound;
    private static Class<?> nbtBase;

    static {
        final String version = Bukkit.getServer().getClass().getName().split("\\.")[3];

        try {
            nmsItemStack = Class.forName("net.minecraft.server." + version + ".ItemStack");
            Class<?> craftItemStack = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
            asNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
            asBukkitCopy = craftItemStack.getMethod("asBukkitCopy", nmsItemStack);
            nbtTagCompound = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
            nbtBase = Class.forName("net.minecraft.server." + version + ".NBTBase");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {}
    }

    public static Class<?> getMinecraftStack() {
        return nmsItemStack;
    }

    public static Class<?> getNBTTagCompound() {
        return nbtTagCompound;
    }

    public static Class<?> getNBTBase() {
        return nbtBase;
    }

    public static Method asNMSCopy() {
        return asNMSCopy;
    }

    public static Method asBukkitCopy() {
        return asBukkitCopy;
    }

    public static boolean isPre1_9() {
        return (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.7"));
    }

    public static boolean isPre1_8() {
        return Bukkit.getVersion().contains("1.7");
    }
}
