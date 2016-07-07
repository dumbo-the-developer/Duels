package me.realized.duels.utilities.compat;

import org.bukkit.Bukkit;

public class CompatHelper {

    private static Class<?> nmsItemStack;
    private static Class<?> craftItemStack;
    private static Class<?> nbtTagCompound;
    private static Class<?> nbtBase;

    static {
        final String version = Bukkit.getServer().getClass().getName().split("\\.")[3];

        try {
            nmsItemStack = Class.forName("net.minecraft.server." + version + ".ItemStack");
            craftItemStack = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
            nbtTagCompound = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
            nbtBase = Class.forName("net.minecraft.server." + version + ".NBTBase");
        } catch (ClassNotFoundException ignored) {}
    }

    public static Class<?> getMinecraftStack() {
        return nmsItemStack;
    }

    public static Class<?> getCraftStack() {
        return craftItemStack;
    }

    public static Class<?> getNBTTagCompound() {
        return nbtTagCompound;
    }

    public static Class<?> getNBTBase() {
        return nbtBase;
    }
}
