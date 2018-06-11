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

import org.bukkit.inventory.ItemStack;

/**
 * Prevents players from keeping kit contents by removing items with the given key on click/interact/pickup
 */
public final class Tags extends CompatBase {

    private Tags() {}

    public static ItemStack setKey(final ItemStack item, final String key) {
        if (item == null) {
            return null;
        }

        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);
            Object tag = GET_TAG.invoke(nmsItem);

            if (tag == null) {
                tag = TAG_COMPOUND.newInstance();
            }

            SET_STRING.invoke(tag, key, "true");
            SET_TAG.invoke(nmsItem, tag);
            return (ItemStack) AS_BUKKIT_COPY.invoke(null, nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
            return item;
        }
    }

    public static boolean hasNoKey(final ItemStack item, final String key) {
        if (item == null) {
            return true;
        }

        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);

            if (nmsItem == null) {
                return true;
            }

            final Object tag = GET_TAG.invoke(nmsItem);
            return tag == null || GET_STRING.invoke(tag, key) == null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }
    }
}
