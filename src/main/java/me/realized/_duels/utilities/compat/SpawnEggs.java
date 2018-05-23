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

package me.realized._duels.utilities.compat;

import java.lang.reflect.Method;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class SpawnEggs {

    private static final Class<?> itemStack = Reflections.getNMSClass("ItemStack");
    private static final Class<?> craftItemStack = Reflections.getCBClass("inventory.CraftItemStack");

    private static final Class<?> nbtTagCompound = Reflections.getNMSClass("NBTTagCompound");
    private static final Class<?> nbtBase = Reflections.getNMSClass("NBTBase");

    private static final Method asNMSCopy = Reflections.getMethod(craftItemStack, "asNMSCopy", ItemStack.class);
    private static final Method asBukkitCopy = Reflections.getMethod(craftItemStack, "asBukkitCopy", itemStack);

    private final EntityType type;

    public SpawnEggs(EntityType type) {
        this.type = type;
    }

    @SuppressWarnings("deprecation")
    public static SpawnEggs fromItemStack(ItemStack item) {
        // Because IDE is complaining
        if (asNMSCopy == null) {
            return null;
        }

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

    public EntityType getType() {
        return type;
    }

    @SuppressWarnings("deprecation")
    public ItemStack toItemStack(int amount) {
        // Because IDE is complaining
        if (asBukkitCopy == null || asNMSCopy == null) {
            return null;
        }

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
}
