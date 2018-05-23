/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.realized.duels.util.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.realized.duels.util.ReflectionUtil;
import org.bukkit.inventory.ItemStack;

class CompatBase {

    static final Method AS_NMS_COPY;
    static final Method AS_BUKKIT_COPY;

    static final Class<?> TAG_COMPOUND;
    static final Method GET_TAG;
    static final Method SET_TAG;
    static final Method GET;
    static final Method SET;
    static final Method ADD;
    static final Method SIZE;
    static final Method GET_LIST;
    static final Method GET_STRING;
    static final Method SET_STRING;
    static final Method GET_INT;
    static final Method SET_INT;
    static final Method GET_DOUBLE;
    static final Method SET_DOUBLE;
    static final Method SET_LONG;
    static final Method GET_COMPOUND;

    static final Method GET_HANDLE;
    static Field COLLIDES_WITH_ENTITIES;

    static {
        final Class<?> CB_ITEMSTACK = ReflectionUtil.getCBClass("inventory.CraftItemStack");
        final Class<?> NMS_ITEMSTACK = ReflectionUtil.getNMSClass("ItemStack");
        AS_NMS_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asNMSCopy", ItemStack.class);
        AS_BUKKIT_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asBukkitCopy", NMS_ITEMSTACK);
        TAG_COMPOUND = ReflectionUtil.getNMSClass("NBTTagCompound");

        final Class<?> TAG_LIST = ReflectionUtil.getNMSClass("NBTTagList");
        final Class<?> TAG_BASE = ReflectionUtil.getNMSClass("NBTBase");
        GET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "getTag");
        SET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "setTag", TAG_COMPOUND);
        SET = ReflectionUtil.getMethod(TAG_COMPOUND, "set", String.class, TAG_BASE);
        GET_LIST = ReflectionUtil.getMethod(TAG_COMPOUND, "getList", String.class, int.class);
        GET = ReflectionUtil.getMethod(TAG_LIST, "get", int.class);
        ADD = ReflectionUtil.getMethod(TAG_LIST, "add", TAG_BASE);
        SIZE = ReflectionUtil.getMethod(TAG_LIST, "size");
        GET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "getString", String.class);
        SET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "setString", String.class, String.class);
        GET_INT = ReflectionUtil.getMethod(TAG_COMPOUND, "getInt", String.class);
        SET_INT = ReflectionUtil.getMethod(TAG_COMPOUND, "setInt", String.class, int.class);
        GET_DOUBLE = ReflectionUtil.getMethod(TAG_COMPOUND, "getDouble", String.class);
        SET_DOUBLE = ReflectionUtil.getMethod(TAG_COMPOUND, "setDouble", String.class, double.class);
        SET_LONG = ReflectionUtil.getMethod(TAG_COMPOUND, "setLong", String.class, long.class);
        GET_COMPOUND = ReflectionUtil.getMethod(TAG_COMPOUND, "getCompound", String.class);
        final Class<?> CB_PLAYER = ReflectionUtil.getCBClass("entity.CraftPlayer");
        GET_HANDLE = ReflectionUtil.getMethod(CB_PLAYER, "getHandle");

        if (CompatUtil.isPre_1_10()) {
            final Class<?> NMS_PLAYER = ReflectionUtil.getNMSClass("EntityPlayer");
            COLLIDES_WITH_ENTITIES = ReflectionUtil.getField(NMS_PLAYER, "collidesWithEntities");
        }
    }
}
