package me.realized.duels.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.realized._duels.Core;
import me.realized._duels.utilities.Helper;
import me.realized.duels.util.compat.Attributes;
import me.realized.duels.util.compat.CompatUtil;
import me.realized.duels.util.compat.Potions;
import me.realized.duels.util.compat.SpawnEggs;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class ItemData {

    private final String material;
    private final int amount;
    private final short data;

    private String itemData;
    private List<AttributeData> attributeModifiers;
    private Map<String, Integer> enchantments;
    private String displayName;
    private List<String> lore;
    private List<String> flags;
    private String owner;
    private String author;
    private String title;
    private List<String> contents;
    private Map<String, String> effects;
    private String color;

    public ItemData(final ItemStack item) {
        this.material = item.getType().name();
        this.amount = item.getAmount() == 0 ? 1 : item.getAmount();
        this.data = item.getDurability();

        final Attributes attributes = new Attributes(item);
        final List<AttributeData> modifiers = attributes.getModifiers();

        if (!modifiers.isEmpty()) {
            this.attributeModifiers = new ArrayList<>();
            attributeModifiers.addAll(modifiers);
        }

        if (!CompatUtil.isPre1_9()) {
            if (material.contains("POTION")) {
                Potions potion = Potions.fromItemStack(item);

                if (potion == null) {
                    return;
                }

                final StringBuilder data = new StringBuilder();
                data.append(potion.getType().name()).append("-");

                if (potion.isExtended()) {
                    data.append("extended-");
                }

                if (potion.isLinger()) {
                    data.append("linger-");
                }

                if (potion.isSplash()) {
                    data.append("splash-");
                }

                if (potion.isStrong()) {
                    data.append("strong-");
                }

                this.itemData = data.toString();
            } else if (material.equals("MONSTER_EGG")) {
                final SpawnEggs spawnEgg = SpawnEggs.fromItemStack(item);

                if (spawnEgg == null) {
                    return;
                }

                this.itemData = spawnEgg.getType().name() + "-";
            } else if (material.equals("TIPPED_ARROW")) {
                final PotionMeta meta = (PotionMeta) item.getItemMeta();
                final PotionData potionData = meta.getBasePotionData();

                if (potionData.getType().getEffectType() == null) {
                    return;
                }

                final StringBuilder data = new StringBuilder();
                data.append(potionData.getType().name()).append("-");

                if (potionData.isExtended()) {
                    data.append("extended-");
                }

                if (potionData.isUpgraded()) {
                    data.append("upgraded-");
                }

                this.itemData = data.toString();
            }
        }

        if (!item.getEnchantments().isEmpty()) {
            this.enchantments = new HashMap<>();

            for (final Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                enchantments.put(entry.getKey().getName(), entry.getValue());
            }
        }

        if (item.hasItemMeta()) {
            final ItemMeta meta = item.getItemMeta();

            if (meta.hasDisplayName()) {
                displayName = meta.getDisplayName();
            }

            if (meta.hasLore()) {
                lore = meta.getLore();
            }

            if (!Helper.isPre1_8() && !meta.getItemFlags().isEmpty()) {
                this.flags = new ArrayList<>();

                for (final ItemFlag flag : meta.getItemFlags()) {
                    flags.add(flag.name());
                }
            }

            if (item.getType() == Material.SKULL_ITEM && item.getDurability() == 3) {
                final SkullMeta skullMeta = (SkullMeta) meta;

                if (skullMeta.hasOwner()) {
                    owner = skullMeta.getOwner();
                }
            }

            if (item.getType() == Material.POTION) {
                this.effects = new HashMap<>();

                final PotionMeta potionMeta = (PotionMeta) meta;

                if (potionMeta.hasCustomEffects()) {
                    for (final PotionEffect effect : potionMeta.getCustomEffects()) {
                        effects.put(effect.getType().getName(), effect.getDuration() + "-" + effect.getAmplifier());
                    }
                }
            }

            if (item.getType() == Material.WRITTEN_BOOK) {
                final BookMeta bookMeta = (BookMeta) meta;

                if (bookMeta.hasAuthor()) {
                    author = bookMeta.getAuthor();
                }

                if (bookMeta.hasTitle()) {
                    title = bookMeta.getTitle();
                }

                if (bookMeta.hasPages()) {
                    contents = bookMeta.getPages();
                }
            }

            if (item.getType().name().contains("LEATHER_")) {
                final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;

                if (DyeColor.getByColor(leatherArmorMeta.getColor()) != null) {
                    color = DyeColor.getByColor(leatherArmorMeta.getColor()).name();
                }
            }
        }
    }

    public ItemStack toItemStack() {
        final Material type = Material.getMaterial(material);

        if (type == null) {
            return null;
        }

        ItemStack item = new ItemStack(type, amount, data);

        if (attributeModifiers != null) {
            item = new Attributes(item).addModifiers(attributeModifiers);
        }

        if (!Helper.isPre1_9() && itemData != null) {
            final List<String> args = Arrays.asList(this.itemData.split("-"));

            if (material.contains("POTION")) {
                item = new Potions(PotionType.valueOf(args.get(0)), args).toItemStack(amount);
            } else if (material.equals("MONSTER_EGG")) {
                item = new SpawnEggs(EntityType.valueOf(args.get(0))).toItemStack(amount);
            } else if (material.equals("TIPPED_ARROW")) {
                final PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                final PotionData data = new PotionData(PotionType.valueOf(args.get(0)), args.contains("extended"), args.contains("upgraded"));
                potionMeta.setBasePotionData(data);
                item.setItemMeta(potionMeta);
            }
        }

        if (enchantments != null && !enchantments.isEmpty()) {
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                item.addUnsafeEnchantment(Enchantment.getByName(entry.getKey()), entry.getValue());
            }
        }

        final ItemMeta meta = item.getItemMeta();

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }

        if (!Helper.isPre1_8() && flags != null && !flags.isEmpty()) {
            for (final String flag : flags) {
                meta.addItemFlags(ItemFlag.valueOf(flag));
            }
        }

        if (item.getType() == Material.SKULL_ITEM && item.getDurability() == 3 && owner != null) {
            final SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwner(owner);
        }

        if (item.getType() == Material.POTION && effects != null && !effects.isEmpty()) {
            final PotionMeta potionMeta = (PotionMeta) meta;

            for (final Map.Entry<String, String> entry : effects.entrySet()) {
                final int duration = Integer.valueOf(entry.getValue().split("-")[0]);
                final int amplifier = Integer.valueOf(entry.getValue().split("-")[1]);
                potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(entry.getKey()), duration, amplifier), true);
            }
        }

        if (item.getType() == Material.WRITTEN_BOOK) {
            final BookMeta bookMeta = (BookMeta) meta;

            if (author != null) {
                bookMeta.setAuthor(author);
            }

            if (title != null) {
                bookMeta.setTitle(title);
            }

            if (contents != null && !contents.isEmpty()) {
                bookMeta.setPages(contents);
            }
        }

        if (item.getType().name().contains("LEATHER_")) {
            final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;

            if (color != null) {
                leatherArmorMeta.setColor(DyeColor.valueOf(color).getColor());
            }
        }

        item.setItemMeta(meta);
        return item;
    }
}
