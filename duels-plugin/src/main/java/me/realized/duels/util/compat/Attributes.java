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

                if (CompatUtil.isPre1_14()) {
                    ADD.invoke(list, tag);
                } else {
                    ADD.invoke(list, 0, tag);
                }
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
