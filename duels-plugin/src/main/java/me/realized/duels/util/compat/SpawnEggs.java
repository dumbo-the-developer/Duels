package me.realized.duels.util.compat;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class SpawnEggs extends CompatBase {

    @Getter
    private final EntityType type;

    public SpawnEggs(EntityType type) {
        this.type = type;
    }

    @SuppressWarnings("deprecation")
    public static SpawnEggs fromItemStack(final ItemStack item) {
        if (item == null || item.getType() != Material.MONSTER_EGG) {
            return null;
        }

        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);
            final Object tag = GET_TAG.invoke(nmsItem);

            if (tag != null) {
                final Object entityTag = GET_COMPOUND.invoke(tag, "EntityTag");
                final String name = ((String) GET_STRING.invoke(entityTag, "id")).replace("minecraft:", "");
                final EntityType type = EntityType.fromName(name);

                if (type != null) {
                    return new SpawnEggs(type);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    public ItemStack toItemStack(final int amount) {
        try {
            final ItemStack item = new ItemStack(Material.MONSTER_EGG, amount);
            Object nmsItem = AS_NMS_COPY.invoke(null, item);
            Object tag = GET_TAG.invoke(nmsItem);

            if (tag == null) {
                tag = TAG_COMPOUND.newInstance();
            }

            final Object id = TAG_COMPOUND.newInstance();
            SET_STRING.invoke(id, "id", type.getName());
            SET.invoke(tag, "EntityTag", id);
            SET_TAG.invoke(nmsItem, tag);
            return (ItemStack) AS_BUKKIT_COPY.invoke(null, nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
