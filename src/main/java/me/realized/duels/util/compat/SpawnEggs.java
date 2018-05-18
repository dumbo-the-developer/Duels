/*
 *
 *   This file is part of TokenManager, licensed under the MIT License.
 *
 *   Copyright (c) Realized
 *   Copyright (c) contributors
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 *
 */

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
