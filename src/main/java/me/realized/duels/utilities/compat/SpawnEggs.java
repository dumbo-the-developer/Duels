package me.realized.duels.utilities.compat;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class SpawnEggs {

    private static final Class<?> itemStack = CompatHelper.getMinecraftStack();
    private static final Class<?> nbtTagCompound = CompatHelper.getNBTTagCompound();
    private static final Class<?> nbtBase = CompatHelper.getNBTBase();
    private static final Method asNMSCopy = CompatHelper.asNMSCopy();
    private static final Method asBukkitCopy = CompatHelper.asBukkitCopy();

    private final EntityType type;

    public SpawnEggs(EntityType type) {
        this.type = type;
    }

    public EntityType getType() {
        return type;
    }

    @SuppressWarnings("deprecation")
    public ItemStack toItemStack(int amount) {
        try {
            ItemStack item = new ItemStack(Material.MONSTER_EGG, amount);
            Object stack = asNMSCopy.invoke(null, item);
            Object tagCompound = itemStack.getMethod("getTag").invoke(stack);

            if (tagCompound == null) {
                tagCompound = nbtTagCompound.newInstance();
            }

            Object id = nbtTagCompound.newInstance();
            nbtTagCompound.getMethod("setString", String.class, String.class).invoke(id, "id", type.getName());
            nbtTagCompound.getMethod("set", String.class, nbtBase).invoke(tagCompound, "EntityTag", id);
            itemStack.getMethod("setTag", nbtTagCompound).invoke(stack, tagCompound);
            return (ItemStack) asBukkitCopy.invoke(null, stack);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public static SpawnEggs fromItemStack(ItemStack item) {
        if (item == null || item.getType() != Material.MONSTER_EGG) {
            throw new IllegalArgumentException("item is not a monster egg");
        }

        try {
            Object stack = asNMSCopy.invoke(null, item);
            Object tagCompound = itemStack.getMethod("getTag").invoke(stack);

            if (tagCompound != null) {
                Object entityTagCompound = nbtTagCompound.getMethod("getCompound", String.class).invoke(tagCompound, "EntityTag");
                String name = (String) nbtTagCompound.getMethod("getString", String.class).invoke(entityTagCompound, "id");
                EntityType type = EntityType.fromName(name);

                if (type != null) {
                    return new SpawnEggs(type);
                }
            }
        } catch (Exception ignored) {}

        return null;
    }
}
