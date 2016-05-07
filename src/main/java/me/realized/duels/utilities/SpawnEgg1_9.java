package me.realized.duels.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class SpawnEgg1_9 {

    private static Class<?> itemStack;
    private static Class<?> nbtTagCompound;
    private static Class<?> nbtBase;
    private static Class<?> craftItemStack;

    static {
        final String version = Bukkit.getServer().getClass().getName().split("\\.")[3];

        try {
            itemStack = Class.forName("net.minecraft.server." + version + ".ItemStack");
            nbtTagCompound = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
            nbtBase = Class.forName("net.minecraft.server." + version + ".NBTBase");
            craftItemStack = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (ClassNotFoundException ignored) {}
    }

    private final EntityType type;

    public SpawnEgg1_9(EntityType type) {
        this.type = type;
    }

    public EntityType getType() {
        return type;
    }

    @SuppressWarnings("deprecation")
    public ItemStack toItemStack(int amount) {
        try {
            ItemStack item = new ItemStack(Material.MONSTER_EGG, amount);
            Object stack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            Object tagCompound = itemStack.getMethod("getTag").invoke(stack);

            if (tagCompound == null) {
                tagCompound = nbtTagCompound.newInstance();
            }

            Object id = nbtTagCompound.newInstance();
            nbtTagCompound.getMethod("setString", String.class, String.class).invoke(id, "id", type.getName());
            nbtTagCompound.getMethod("set", String.class, nbtBase).invoke(tagCompound, "EntityTag", id);
            itemStack.getMethod("setTag", nbtTagCompound).invoke(stack, tagCompound);
            return (ItemStack) craftItemStack.getMethod("asBukkitCopy", itemStack).invoke(null, stack);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public static SpawnEgg1_9 fromItemStack(ItemStack item) {
        if (item == null || item.getType() != Material.MONSTER_EGG) {
            throw new IllegalArgumentException("item is not a monster egg");
        }

        try {
            Object stack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            Object tagCompound = itemStack.getMethod("getTag").invoke(stack);

            if (tagCompound != null) {
                Object entityTagCompound = nbtTagCompound.getMethod("getCompound", String.class).invoke(tagCompound, "EntityTag");
                String name = (String) nbtTagCompound.getMethod("getString", String.class).invoke(entityTagCompound, "id");
                EntityType type = EntityType.fromName(name);

                if (type != null) {
                    return new SpawnEgg1_9(type);
                }
            }
        } catch (Exception ignored) {}

        return null;
    }
}
