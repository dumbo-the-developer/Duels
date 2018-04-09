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

package me.realized.duels.util.inventory;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.Consumer;
import me.realized.duels.util.EnumUtil;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.Potions;
import me.realized.duels.util.compat.SpawnEggs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public final class ItemUtil {

    private static final Map<String, Enchantment> ENCHANTMENTS = new HashMap<>();
    private static final Map<String, PotionEffectType> EFFECTS = new HashMap<>();

    static {
        registerEnchantment("power", Enchantment.ARROW_DAMAGE);
        registerEnchantment("flame", Enchantment.ARROW_FIRE);
        registerEnchantment("infinity", Enchantment.ARROW_INFINITE);
        registerEnchantment("punch", Enchantment.ARROW_KNOCKBACK);
        registerEnchantment("sharpness", Enchantment.DAMAGE_ALL);
        registerEnchantment("baneofarthopods", Enchantment.DAMAGE_ARTHROPODS);
        registerEnchantment("smite", Enchantment.DAMAGE_UNDEAD);
        registerEnchantment("efficiency", Enchantment.DIG_SPEED);
        registerEnchantment("unbreaking", Enchantment.DURABILITY);
        registerEnchantment("thorns", Enchantment.THORNS);
        registerEnchantment("fireaspect", Enchantment.FIRE_ASPECT);
        registerEnchantment("knockback", Enchantment.KNOCKBACK);
        registerEnchantment("fortune", Enchantment.LOOT_BONUS_BLOCKS);
        registerEnchantment("looting", Enchantment.LOOT_BONUS_MOBS);
        registerEnchantment("respiration", Enchantment.OXYGEN);
        registerEnchantment("blastprotection", Enchantment.PROTECTION_EXPLOSIONS);
        registerEnchantment("featherfalling", Enchantment.PROTECTION_FALL);
        registerEnchantment("fireprotection", Enchantment.PROTECTION_FIRE);
        registerEnchantment("projectileprotection", Enchantment.PROTECTION_PROJECTILE);
        registerEnchantment("protection", Enchantment.PROTECTION_ENVIRONMENTAL);
        registerEnchantment("silktouch", Enchantment.SILK_TOUCH);
        registerEnchantment("aquaaffinity", Enchantment.WATER_WORKER);
        registerEnchantment("luck", Enchantment.LUCK);
        registerEnchantment("lure", Enchantment.LURE);

        registerEffect("speed", PotionEffectType.SPEED);
        registerEffect("slowness", PotionEffectType.SLOW);
        registerEffect("haste", PotionEffectType.FAST_DIGGING);
        registerEffect("fatigue", PotionEffectType.SLOW_DIGGING);
        registerEffect("strength", PotionEffectType.INCREASE_DAMAGE);
        registerEffect("heal", PotionEffectType.HEAL);
        registerEffect("harm", PotionEffectType.HARM);
        registerEffect("jump", PotionEffectType.JUMP);
        registerEffect("nausea", PotionEffectType.CONFUSION);
        registerEffect("regeneration", PotionEffectType.REGENERATION);
        registerEffect("resistance", PotionEffectType.DAMAGE_RESISTANCE);
        registerEffect("fireresistance", PotionEffectType.FIRE_RESISTANCE);
        registerEffect("waterbreathing", PotionEffectType.WATER_BREATHING);
        registerEffect("invisibility", PotionEffectType.INVISIBILITY);
        registerEffect("blindness", PotionEffectType.BLINDNESS);
        registerEffect("nightvision", PotionEffectType.NIGHT_VISION);
        registerEffect("hunger", PotionEffectType.HUNGER);
        registerEffect("weakness", PotionEffectType.WEAKNESS);
        registerEffect("poison", PotionEffectType.POISON);
        registerEffect("wither", PotionEffectType.WITHER);
        registerEffect("healthboost", PotionEffectType.HEALTH_BOOST);
        registerEffect("absorption", PotionEffectType.ABSORPTION);
        registerEffect("saturation", PotionEffectType.SATURATION);
    }

    private ItemUtil() {}

    private static void registerEnchantment(final String key, final Enchantment value) {
        ENCHANTMENTS.put(key, value);
        ENCHANTMENTS.put(value.getName(), value);
    }

    private static void registerEffect(final String key, final PotionEffectType value) {
        EFFECTS.put(key, value);
        EFFECTS.put(value.getName(), value);
    }

    public static ItemStack replace(final ItemStack item, final String placeholder, final Object value) {
        final ItemMeta meta = item.getItemMeta();
        final List<String> lore = meta.getLore();
        lore.replaceAll(line -> line.replace(placeholder, value.toString()));
        meta.setLore(lore);
        meta.setDisplayName(meta.getDisplayName().replace(placeholder, value.toString()));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack loadFromString(final String line) {
        if (line == null || line.isEmpty()) {
            throw new IllegalArgumentException("Line is empty or null!");
        }

        final String[] args = line.split(" +");
        final String[] materialData = args[0].split(":");
        final Material material = Material.matchMaterial(materialData[0]);

        if (material == null) {
            throw new IllegalArgumentException("'" + args[0] + "' is not a valid material.");
        }

        ItemStack result = new ItemStack(material, 1);

        if (materialData.length > 1) {
            // Handle potions and spawn eggs switching to NBT in 1.9+
            if (!ItemUtil.isPre1_9()) {
                if (material.name().contains("POTION")) {
                    final String[] values = materialData[1].split("-");
                    final PotionType type;

                    if ((type = EnumUtil.getByName(values[0], PotionType.class)) == null) {
                        throw new IllegalArgumentException("'" + values[0] + "' is not a valid PotionType. Available: " + EnumUtil.getNames(PotionType.class));
                    }

                    result = new Potions(type, Arrays.asList(values)).toItemStack();
                } else if (material == Material.MONSTER_EGG) {
                    final EntityType type;

                    if ((type = EnumUtil.getByName(materialData[1], EntityType.class)) == null) {
                        throw new IllegalArgumentException("'" + materialData[0] + "' is not a valid EntityType. Available: " + EnumUtil.getNames(EntityType.class));
                    }

                    result = new SpawnEggs(type).toItemStack();
                }
            }

            final OptionalLong value;

            if ((value = NumberUtil.parseLong(materialData[1])).isPresent()) {
                result.setDurability((short) value.getAsLong());
            }
        }

        if (args.length < 2) {
            return result;
        }

        result.setAmount(Integer.parseInt(args[1]));

        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                final String argument = args[i];
                final String[] pair = argument.split(":", 2);

                if (pair.length < 2) {
                    continue;
                }

                applyMeta(result, pair[0], pair[1]);
            }
        }

        return result;
    }


    public static ItemStack loadFromString(final String line, final Consumer<String> errorHandler) {
        ItemStack result;

        try {
            result = loadFromString(line);
        } catch (Exception ex) {
            result = ItemBuilder
                .of(Material.REDSTONE_BLOCK)
                .name("&4&m------------------")
                .lore(
                    "&cThere was an error",
                    "&cwhile loading this",
                    "&citem, please contact",
                    "&can administrator.",
                    "&4&m------------------"
                )
                .build();
            errorHandler.accept(ex.getMessage());
        }

        return result;
    }

    private static void applyMeta(final ItemStack item, final String key, final String value) {
        final ItemMeta meta = item.getItemMeta();

        if (key.equalsIgnoreCase("name")) {
            meta.setDisplayName(StringUtil.color(value.replace("_", " ")));
            item.setItemMeta(meta);
            return;
        }

        if (key.equalsIgnoreCase("lore")) {
            meta.setLore(StringUtil.color(Lists.newArrayList(value.split("\\|")), s -> s.replace("_", " ")));
            item.setItemMeta(meta);
            return;
        }

        if (key.equalsIgnoreCase("unbreakable") && value.equalsIgnoreCase("true")) {
            if (isPre1_12()) {
                meta.spigot().setUnbreakable(true);
            } else {
                meta.setUnbreakable(true);
            }

            item.setItemMeta(meta);
            return;
        }

        final Enchantment enchantment = ENCHANTMENTS.get(key);

        if (enchantment != null) {
            item.addUnsafeEnchantment(enchantment, Integer.parseInt(value));
            return;
        }

        if (item.getType().name().contains("POTION")) {
            final PotionEffectType effectType = EFFECTS.get(key);

            if (effectType != null) {
                String[] values = value.split(":");
                PotionMeta potionMeta = (PotionMeta) meta;
                potionMeta.addCustomEffect(new PotionEffect(effectType, Integer.parseInt(values[1]), Integer.parseInt(values[0])), true);
                item.setItemMeta(potionMeta);
                return;
            }
        }

        if (item.getType() == Material.SKULL_ITEM && item.getDurability() == 3 && key.equalsIgnoreCase("player") || key
            .equalsIgnoreCase("owner")) {
            final SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwner(value);
            item.setItemMeta(skullMeta);
        }
    }

    private static boolean isPre1_9() {
        return getVersion().contains("1.7") || getVersion().contains("1.8");
    }

    private static boolean isPre1_12() {
        return isPre1_9() || getVersion().contains("1.9") || getVersion().contains("1.10") || getVersion().contains("1.11");
    }

    private static String getVersion() {
        return Bukkit.getVersion();
    }
}
