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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.realized.duels.data.AttributeData;
import org.bukkit.inventory.ItemStack;

public class Attributes extends CompatBase {

    private ItemStack item;
    private Object list;

    public Attributes(ItemStack item) {
        this.item = item;

        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);

            if (nmsItem == null) {
                return;
            }

            Object tag = GET_TAG.invoke(nmsItem);

            if (tag == null) {
                tag = TAG_COMPOUND.newInstance();
            }

            final Object attributes = GET_LIST.invoke(tag, "AttributeModifiers", 10);

            if (attributes == null) {
                return;
            }

            this.list = attributes;
        } catch (Exception ignored) {}
    }

    public ItemStack addModifiers(final List<AttributeData> attributes) {
        try {
            for (final AttributeData data : attributes) {
                final Object tag = from(data);

                if (tag == null) {
                    continue;
                }

                ADD.invoke(list, tag);
            }

            final Object nmsItem = AS_NMS_COPY.invoke(null, item);

            if (nmsItem == null) {
                return item;
            }

            Object tag = GET_TAG.invoke(nmsItem);

            if (tag == null) {
                tag = TAG_COMPOUND.newInstance();
            }

            SET.invoke(tag, "AttributeModifiers", list);
            SET_TAG.invoke(nmsItem, tag);
            return this.item = (ItemStack) AS_BUKKIT_COPY.invoke(null, nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
            return item;
        }
    }

    private Object from(final AttributeData data) {
        try {
            final Object tag = TAG_COMPOUND.newInstance();
            SET_STRING.invoke(tag, "Name", data.getName());
            SET_STRING.invoke(tag, "AttributeName", data.getAttrName());
            SET_INT.invoke(tag, "Operation", data.getOperation());
            SET_DOUBLE.invoke(tag, "Amount", data.getAmount());

            final UUID random = UUID.randomUUID();
            SET_LONG.invoke(tag, "UUIDMost", random.getMostSignificantBits());
            SET_LONG.invoke(tag, "UUIDLeast", random.getLeastSignificantBits());

            final String slot = data.getSlot();

            if (slot != null && !slot.equals("")) {
                SET_STRING.invoke(tag, "Slot", slot);
            }

            return tag;
        } catch (Exception ex) {
            return null;
        }
    }

    public List<AttributeData> getModifiers() {
        List<AttributeData> result = new ArrayList<>();

        if (list == null) {
            return result;
        }

        try {
            for (int i = 0; i < (int) SIZE.invoke(list); i++) {
                final Object tag = GET.invoke(list, i);
                final String name = (String) GET_STRING.invoke(tag, "Name");
                final String attrName = (String) GET_STRING.invoke(tag, "AttributeName");
                final int operation = (int) GET_INT.invoke(tag, "Operation");
                final double amount = (double) GET_DOUBLE.invoke(tag, "Amount");
                final AttributeData modifier = new AttributeData(name, attrName, operation, amount);
                final Object slot = GET_STRING.invoke(tag, "Slot");

                if (slot != null && !slot.equals("")) {
                    modifier.setSlot((String) slot);
                }

                result.add(modifier);
            }
        } catch (Exception ignored) {}
        return result;
    }
}
