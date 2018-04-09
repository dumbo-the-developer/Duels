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

import java.lang.reflect.Method;
import me.realized.duels.util.ReflectionUtil;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class SpawnEggs {

    private static final Method AS_NMS_COPY;
    private static final Class<?> TAG_COMPOUND;
    private static final Method SET;
    private static final Method SET_STRING;
    private static final Method GET_TAG;
    private static final Method SET_TAG;
    private static final Method AS_BUKKIT_COPY;

    static {
        final Class<?> CB_ITEMSTACK;
        AS_NMS_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK = ReflectionUtil.getCBClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class);
        TAG_COMPOUND = ReflectionUtil.getNMSClass("NBTTagCompound");
        SET = ReflectionUtil.getMethod(TAG_COMPOUND, "set", String.class, ReflectionUtil.getNMSClass("NBTBase"));
        SET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "setString", String.class, String.class);
        final Class<?> NMS_ITEMSTACK;
        GET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK = ReflectionUtil.getNMSClass("ItemStack"), "getTag");
        SET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "setTag", TAG_COMPOUND);
        AS_BUKKIT_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asBukkitCopy", NMS_ITEMSTACK);
    }

    private final EntityType type;

    public SpawnEggs(EntityType type) {
        this.type = type;
    }

    @SuppressWarnings("deprecation")
    public ItemStack toItemStack() {
        try {
            ItemStack item = new ItemStack(Material.MONSTER_EGG);
            Object stack = AS_NMS_COPY.invoke(null, item);
            Object tagCompound = GET_TAG.invoke(stack);

            if (tagCompound == null) {
                tagCompound = TAG_COMPOUND.newInstance();
            }

            Object id = TAG_COMPOUND.newInstance();
            SET_STRING.invoke(id, "id", type.getName());
            SET.invoke(tagCompound, "EntityTag", id);
            SET_TAG.invoke(stack, tagCompound);
            return (ItemStack) AS_BUKKIT_COPY.invoke(null, stack);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
