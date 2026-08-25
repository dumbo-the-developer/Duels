package com.meteordevelopments.duels.gui.configuration;

import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.util.compat.CompatUtil;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.compat.Panes;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Getter
@Setter
public class GuiItemConfig {

    private String material = "STONE";
    private short data = 0;
    private int amount = 1;
    private String name;
    private List<String> lore = new ArrayList<>();
    private List<String> loreNoPermission = new ArrayList<>();
    private boolean glowing = false;
    private int customModelData = 0;
    private final List<Integer> slots = new ArrayList<>();
    private final List<Integer> glowingSlots = new ArrayList<>();

    public GuiItemConfig() {
    }

    public GuiItemConfig(final String material, final short data, final String name) {
        this.material = material;
        this.data = data;
        this.name = name;
    }

    public boolean isGlowingAt(final int slot) {
        return glowing || glowingSlots.contains(slot);
    }

    /**
     * Parses slot specifications from an object (Integer, String range "0-8", or List of mixed types).
     */
    public static List<Integer> parseSlots(final Object obj) {
        final Set<Integer> result = new LinkedHashSet<>();
        if (obj == null) {
            return new ArrayList<>();
        }

        if (obj instanceof Number) {
            result.add(((Number) obj).intValue());
        } else if (obj instanceof String) {
            parseSlotString((String) obj, result);
        } else if (obj instanceof List<?>) {
            for (final Object element : (List<?>) obj) {
                if (element instanceof Number) {
                    result.add(((Number) element).intValue());
                } else if (element instanceof String) {
                    parseSlotString((String) element, result);
                }
            }
        }

        return new ArrayList<>(result);
    }

    private static void parseSlotString(final String str, final Set<Integer> result) {
        final String trimmed = str.trim();
        if (trimmed.contains("-")) {
            final String[] split = trimmed.split("-");
            if (split.length == 2) {
                try {
                    final int start = Integer.parseInt(split[0].trim());
                    final int end = Integer.parseInt(split[1].trim());
                    final int min = Math.min(start, end);
                    final int max = Math.max(start, end);
                    for (int i = min; i <= max; i++) {
                        result.add(i);
                    }
                    return;
                } catch (NumberFormatException ignored) {
                }
            }
        }

        try {
            result.add(Integer.parseInt(trimmed));
        } catch (NumberFormatException ignored) {
        }
    }

    /**
     * Parses a GuiItemConfig from a ConfigurationSection.
     */
    public static GuiItemConfig parse(final ConfigurationSection section) {
        if (section == null) {
            return new GuiItemConfig();
        }

        final GuiItemConfig config = new GuiItemConfig();

        // Support direct slot/slots or nested item section
        if (section.contains("slot")) {
            config.getSlots().addAll(parseSlots(section.get("slot")));
        }
        if (section.contains("slots")) {
            config.getSlots().addAll(parseSlots(section.get("slots")));
        }

        if (section.contains("glowing-slot")) {
            config.getGlowingSlots().addAll(parseSlots(section.get("glowing-slot")));
        }
        if (section.contains("glowing-slots")) {
            config.getGlowingSlots().addAll(parseSlots(section.get("glowing-slots")));
        }

        // If nested under 'item:', load from that subsection; otherwise load from current section
        final ConfigurationSection itemSec = section.isConfigurationSection("item") ? section.getConfigurationSection("item") : section;

        if (itemSec != null) {
            config.setMaterial(itemSec.getString("material", itemSec.getString("type", config.getMaterial())));
            config.setData((short) itemSec.getInt("data", itemSec.getInt("durability", 0)));
            config.setAmount(Math.max(1, itemSec.getInt("amount", 1)));
            config.setName(itemSec.getString("name"));

            if (itemSec.isList("lore")) {
                config.setLore(itemSec.getStringList("lore"));
            } else if (itemSec.isString("lore")) {
                config.setLore(Collections.singletonList(itemSec.getString("lore")));
            }

            if (itemSec.isList("lore-no-permission")) {
                config.setLoreNoPermission(itemSec.getStringList("lore-no-permission"));
            } else if (itemSec.isString("lore-no-permission")) {
                config.setLoreNoPermission(Collections.singletonList(itemSec.getString("lore-no-permission")));
            }

            config.setGlowing(itemSec.getBoolean("glowing", false));
            config.setCustomModelData(itemSec.getInt("custom-model-data", 0));
        }

        return config;
    }

    /**
     * Builds the ItemStack for this configuration.
     */
    public ItemStack buildItem(final Lang lang) {
        return buildItem(lang, this.glowing, Collections.emptyMap());
    }

    /**
     * Builds the ItemStack for this configuration with glow override.
     */
    public ItemStack buildItem(final Lang lang, final boolean glow) {
        return buildItem(lang, glow, Collections.emptyMap());
    }

    /**
     * Builds the ItemStack for this configuration with placeholder replacements.
     */
    public ItemStack buildItem(final Lang lang, final boolean glow, final Map<String, String> placeholders) {
        final ItemStack base = createBaseItemStack();
        final ItemMeta meta = base.getItemMeta();

        if (meta != null) {
            if (name != null) {
                String formattedName = formatPlaceholders(name, placeholders);
                meta.setDisplayName(lang.toLegacyString(formattedName));
            }

            if (lore != null && !lore.isEmpty()) {
                final List<String> formattedLore = new ArrayList<>();
                for (final String line : lore) {
                    final String replaced = formatPlaceholders(line, placeholders);
                    formattedLore.add(lang.toLegacyString(replaced));
                }
                meta.setLore(formattedLore);
            }

            if (glow) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                if (CompatUtil.hasItemFlag()) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }

            if (customModelData > 0 && !CompatUtil.isPre1_14()) {
                meta.setCustomModelData(customModelData);
            }

            base.setItemMeta(meta);
        }

        return base;
    }

    /**
     * Builds the ItemStack for this configuration with key-value placeholder pairs.
     */
    public ItemStack buildItem(final Lang lang, final boolean glow, final Object... placeholderPairs) {
        final Map<String, String> placeholders = new HashMap<>();
        for (int i = 0; i < placeholderPairs.length; i += 2) {
            if (i + 1 < placeholderPairs.length) {
                placeholders.put(String.valueOf(placeholderPairs[i]), String.valueOf(placeholderPairs[i + 1]));
            }
        }
        return buildItem(lang, glow, placeholders);
    }

    private ItemStack createBaseItemStack() {
        final String matName = material != null ? material.trim() : "STONE";

        // Special case for stained glass panes across versions
        if (matName.equalsIgnoreCase("STAINED_GLASS_PANE")) {
            if (!CompatUtil.isPre1_13()) {
                final Material paneMat = Panes.from(data);
                return new ItemStack(paneMat != null ? paneMat : Material.GLASS_PANE, amount);
            }
        }

        // Special case for signs
        if (matName.equalsIgnoreCase("SIGN") || matName.equalsIgnoreCase("SIGN_POST") || matName.equalsIgnoreCase("OAK_SIGN")) {
            final Material signMat = CompatUtil.isPre1_14() ? Material.matchMaterial("SIGN") : Material.matchMaterial("OAK_SIGN");
            if (signMat != null) {
                return new ItemStack(signMat, amount);
            }
        }

        // Special case for skull / player head
        if (matName.equalsIgnoreCase("SKULL_ITEM") || matName.equalsIgnoreCase("PLAYER_HEAD") || matName.equalsIgnoreCase("HEAD")) {
            final Material headMat = CompatUtil.isPre1_13() ? Material.matchMaterial("SKULL_ITEM") : Material.matchMaterial("PLAYER_HEAD");
            if (headMat != null) {
                final ItemStack head = new ItemStack(headMat, amount);
                if (CompatUtil.isPre1_13()) {
                    Items.setDurability(head, (short) 3);
                }
                return head;
            }
        }

        final Material mat = resolveMaterial(matName);
        final ItemStack item = new ItemStack(mat != null ? mat : Material.STONE, amount);
        if (data > 0) {
            Items.setDurability(item, data);
        }
        return item;
    }

    private Material resolveMaterial(final String name) {
        if (name == null || name.isEmpty()) {
            return Material.STONE;
        }

        // 1. Direct match
        Material mat = Material.matchMaterial(name);
        if (mat != null) {
            return mat;
        }

        // 2. Try matchMaterial(name, true) via reflection for legacy support on 1.13+
        try {
            final java.lang.reflect.Method method = Material.class.getMethod("matchMaterial", String.class, boolean.class);
            mat = (Material) method.invoke(null, name, true);
            if (mat != null) {
                return mat;
            }
        } catch (Throwable ignored) {
        }

        // 3. Try prefixing LEGACY_
        mat = Material.matchMaterial("LEGACY_" + name.toUpperCase());
        if (mat != null) {
            return mat;
        }

        // 4. Common name aliases
        final String upper = name.toUpperCase();
        switch (upper) {
            case "SIGN":
            case "SIGN_POST":
            case "OAK_SIGN":
                return CompatUtil.isPre1_14() ? Material.matchMaterial("SIGN") : Material.matchMaterial("OAK_SIGN");
            case "GRASS":
            case "GRASS_BLOCK":
                return CompatUtil.isPre1_13() ? Material.matchMaterial("GRASS") : Material.matchMaterial("GRASS_BLOCK");
            case "MUSHROOM_SOUP":
            case "MUSHROOM_STEW":
                return Items.MUSHROOM_SOUP;
            case "GOLD_SWORD":
            case "GOLDEN_SWORD":
                return CompatUtil.isPre1_13() ? Material.matchMaterial("GOLD_SWORD") : Material.matchMaterial("GOLDEN_SWORD");
            case "GOLD_INGOT":
            case "GOLDEN_INGOT":
                return Material.matchMaterial("GOLD_INGOT");
            case "GOLD_APPLE":
            case "GOLDEN_APPLE":
                return Material.matchMaterial("GOLDEN_APPLE");
            default:
                break;
        }

        return Material.STONE;
    }

    private String formatPlaceholders(String text, final Map<String, String> placeholders) {
        if (text == null || placeholders == null || placeholders.isEmpty()) {
            return text;
        }

        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("%" + entry.getKey() + "%", entry.getValue() != null ? entry.getValue() : "");
        }

        return text;
    }
}
