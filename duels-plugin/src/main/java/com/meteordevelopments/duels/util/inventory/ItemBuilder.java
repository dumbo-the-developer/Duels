package com.meteordevelopments.duels.util.inventory;

import com.meteordevelopments.duels.util.EnumUtil;
import com.meteordevelopments.duels.util.StringUtil;
import com.meteordevelopments.duels.util.compat.CompatUtil;
import com.meteordevelopments.duels.util.compat.Items;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class ItemBuilder {

    private final ItemStack result;

    private ItemBuilder(final Material type, final int amount, final short durability) {
        this.result = new ItemStack(type, amount);
        Items.setDurability(result, durability);
    }

    private ItemBuilder(final String type, final int amount, final short durability) {
        this(Material.matchMaterial(type), amount, durability);
    }

    private ItemBuilder(final ItemStack item) {
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
        return new ItemBuilder(type, amount, durability);
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
        return editMeta(meta -> meta.setDisplayName(StringUtil.color(name)));
    }

    public ItemBuilder lore(final List<String> lore) {
        return editMeta(meta -> meta.setLore(StringUtil.color(lore)));
    }

    public ItemBuilder lore(final String... lore) {
        return lore(Arrays.asList(lore));
    }

    /**
     * Sets the display name with MiniMessage support.
     * Requires a Lang instance to check MiniMessage configuration.
     */
    public ItemBuilder name(final String name, final com.meteordevelopments.duels.config.Lang lang) {
        return editMeta(meta -> meta.setDisplayName(lang.toLegacyString(name)));
    }

    /**
     * Sets the lore with MiniMessage support.
     * Requires a Lang instance to check MiniMessage configuration.
     */
    public ItemBuilder lore(final List<String> lore, final com.meteordevelopments.duels.config.Lang lang) {
        return editMeta(meta -> {
            List<String> convertedLore = new ArrayList<>();
            for (String line : lore) {
                convertedLore.add(lang.toLegacyString(line));
            }
            meta.setLore(convertedLore);
        });
    }

    /**
     * Sets the lore with MiniMessage support.
     * Requires a Lang instance to check MiniMessage configuration.
     */
    public ItemBuilder lore(final com.meteordevelopments.duels.config.Lang lang, final String... lore) {
        return lore(Arrays.asList(lore), lang);
    }

    public ItemBuilder enchant(final Enchantment enchantment, final int level) {
        result.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder unbreakable() {
        return editMeta(meta -> {
            if (CompatUtil.isPre1_12()) {
                meta.setUnbreakable(true);
            } else {
                meta.setUnbreakable(true);
            }
        });
    }

    public ItemBuilder head(final String owner) {
        return editMeta(meta -> {
            if (owner != null && Items.equals(Items.HEAD, result)) {
                final SkullMeta skullMeta = (SkullMeta) meta;
                skullMeta.setOwner(owner);
            }
        });
    }

    public ItemBuilder leatherArmorColor(final String color) {
        return editMeta(meta -> {
            final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;

            if (color != null) {
                leatherArmorMeta.setColor(DyeColor.valueOf(color).getColor());
            }
        });
    }

    public ItemBuilder potion(final PotionType type, final boolean extended, final boolean upgraded) {
        PotionMeta meta = (PotionMeta) result.getItemMeta();
        meta.setBasePotionData(new PotionData(type, extended, upgraded));
        result.setItemMeta(meta);
        return this;
    }

    public ItemBuilder attribute(final String name, final int operation, final double amount, final String slotName) {
        return editMeta(meta -> {
            final Attribute attribute = EnumUtil.getByName(attributeNameToEnum(name), Attribute.class);

            if (attribute == null) {
                return;
            }

            final AttributeModifier modifier;

            if (slotName != null) {
                final EquipmentSlot slot = EnumUtil.getByName(slotName, EquipmentSlot.class);

                if (slot == null) {
                    return;
                }

                modifier = new AttributeModifier(UUID.randomUUID(), name, amount, AttributeModifier.Operation.values()[operation], slot);
            } else {
                modifier = new AttributeModifier(UUID.randomUUID(), name, amount, AttributeModifier.Operation.values()[operation]);
            }

            meta.addAttributeModifier(attribute, modifier);
        });
    }

    private String attributeNameToEnum(String name) {
        int len = name.length();
        int capitalLetterIndex = -1;

        for (int i = 0; i < len; i++) {
            if (Character.isUpperCase(name.charAt(i))) {
                capitalLetterIndex = i;
                break;
            }
        }

        if (capitalLetterIndex != -1) {
            name = name.substring(0, capitalLetterIndex) + "_" + name.substring(capitalLetterIndex);
        }

        return name.replace(".", "_").toUpperCase();
    }

    public ItemStack build() {
        return result;
    }
}