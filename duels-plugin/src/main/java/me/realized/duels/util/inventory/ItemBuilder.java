package me.realized.duels.util.inventory;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class ItemBuilder {

    private final ItemStack result;

    private ItemBuilder(final Material type, final int amount, final short durability) {
        if (type == null) {
            throw new IllegalArgumentException("Material cannot be null");
        }
        this.result = new ItemStack(type, amount);
        result.setDurability(durability);
    }

    private ItemBuilder(final String type, final int amount, final short durability) {
        this(Material.matchMaterial(type), amount, durability);
    }

    private ItemBuilder(final ItemStack item) {
        if (item == null || item.getType() == null) {
            throw new IllegalArgumentException("ItemStack or Material cannot be null");
        }
        this.result = item;
    }

    public static ItemBuilder of(final Material type) {
        return of(type, 1);
    }

    public static ItemBuilder of(final Material type, final int amount) {
        return of(type, amount, (short) 0);
    }

    public static ItemBuilder of(final Material type, final int amount, final short durability) {
        return new ItemBuilder(type, amount, durability);
    }

    public static ItemBuilder of(final String type, final int amount, final short durability) {
        Material material = Material.matchMaterial(type);
        if (material == null) {
            System.out.println("Debug: Invalid material type: " + type);
            throw new IllegalArgumentException("Invalid material type: " + type);
        }
        return new ItemBuilder(material, amount, durability);
    }

    public static ItemBuilder of(final ItemStack item) {
        return new ItemBuilder(item);
    }

    public ItemBuilder editMeta(final Consumer<ItemMeta> consumer) {
        final ItemMeta meta = result.getItemMeta();
        consumer.accept(meta);
        result.setItemMeta(meta);
        return this;
    }

    public ItemBuilder name(final String name) {
        return editMeta(meta -> meta.setDisplayName(name));
    }

    public ItemBuilder lore(final List<String> lore) {
        return editMeta(meta -> meta.setLore(lore));
    }

    public ItemBuilder lore(final String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder enchant(final Enchantment enchantment, final int level) {
        result.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder unbreakable() {
        return editMeta(meta -> meta.setUnbreakable(true));
    }

    public ItemBuilder head(final String owner) {
        return editMeta(meta -> {
            if (meta instanceof SkullMeta) {
                ((SkullMeta) meta).setOwner(owner);
            }
        });
    }

    public ItemBuilder leatherArmorColor(final String color) {
        return editMeta(meta -> {
            if (meta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) meta).setColor(Color.fromRGB(Integer.parseInt(color, 16)));
            }
        });
    }

    public ItemBuilder potion(final PotionType type, final boolean extended, final boolean upgraded) {
        return editMeta(meta -> {
            if (meta instanceof PotionMeta) {
                ((PotionMeta) meta).setBasePotionData(new PotionData(type, extended, upgraded));
            }
        });
    }

    public ItemBuilder attribute(final String name, final int operation, final double amount, final String slotName) {
        return editMeta(meta -> {
            Attribute attribute = Attribute.valueOf(name.toUpperCase());
            Operation op = Operation.values()[operation];
            UUID uuid = UUID.randomUUID();
            AttributeModifier modifier;

            if (slotName != null) {
                modifier = new AttributeModifier(uuid, name, amount, op, org.bukkit.inventory.EquipmentSlot.valueOf(slotName.toUpperCase()));
            } else {
                modifier = new AttributeModifier(uuid, name, amount, op);
            }

            meta.addAttributeModifier(attribute, modifier);
        });
    }

    public ItemStack build() {
        return result;
    }
}