package com.meteordevelopments.duels.core.customkit.validation;

import com.google.common.collect.Multimap;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.api.customkit.CustomKit;
import com.meteordevelopments.duels.core.customkit.config.CustomKitsConfig;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class CustomKitValidator {

    @Getter
    public static class ValidationResult {
        private final boolean valid;
        private final String messageKey;
        private final Object[] replacers;

        private ValidationResult(final boolean valid, final String messageKey, final Object... replacers) {
            this.valid = valid;
            this.messageKey = messageKey;
            this.replacers = replacers;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(final String messageKey, final Object... replacers) {
            return new ValidationResult(false, messageKey, replacers);
        }
    }

    private final DuelsPlugin plugin;

    public CustomKitValidator(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public static boolean isValidArmorForSlot(final Material material, final int armorSlot) {
        if (material == null || material.isAir()) {
            return true;
        }
        final String name = material.name();
        switch (armorSlot) {
            case 0: // Helmet / Head
                return name.endsWith("_HELMET") || name.equals("TURTLE_HELMET") || name.equals("CARVED_PUMPKIN")
                        || name.endsWith("_SKULL") || name.endsWith("_HEAD");
            case 1: // Chestplate / Chest
                return name.endsWith("_CHESTPLATE") || name.equals("ELYTRA");
            case 2: // Leggings / Legs
                return name.endsWith("_LEGGINGS");
            case 3: // Boots / Feet
                return name.endsWith("_BOOTS");
            default:
                return false;
        }
    }

    public static String getArmorSlotName(final int armorSlot) {
        switch (armorSlot) {
            case 0: return "Helmet";
            case 1: return "Chestplate";
            case 2: return "Leggings";
            case 3: return "Boots";
            default: return "Armor";
        }
    }

    public ValidationResult validateName(final String name, final UUID owner, final UUID currentKitId, final CustomKitsConfig config) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.fail("ERROR.customkits.name-empty");
        }

        final String trimmed = name.trim();

        if (trimmed.length() < config.getMinNameLength()) {
            return ValidationResult.fail("ERROR.customkits.name-too-short", "min", config.getMinNameLength());
        }

        if (trimmed.length() > config.getMaxNameLength()) {
            return ValidationResult.fail("ERROR.customkits.name-too-long", "max", config.getMaxNameLength());
        }

        if (!config.isAllowSpaces() && trimmed.contains(" ")) {
            return ValidationResult.fail("ERROR.customkits.name-no-spaces");
        }

        try {
            final Pattern pattern = Pattern.compile(config.getAllowedPattern());
            if (!pattern.matcher(trimmed).matches()) {
                return ValidationResult.fail("ERROR.customkits.name-invalid-chars");
            }
        } catch (Exception ignored) {
        }

        // Check duplicate name for this owner
        if (owner != null && plugin.getCustomKitManager() != null) {
            for (final CustomKit existing : plugin.getCustomKitManager().getKits(owner)) {
                if (currentKitId != null && existing.getUniqueId().equals(currentKitId)) {
                    continue;
                }
                if (existing.getName().trim().equalsIgnoreCase(trimmed)) {
                    return ValidationResult.fail("ERROR.customkits.name-duplicate", "name", trimmed);
                }
            }
        }

        return ValidationResult.ok();
    }

    public ValidationResult validateItem(final ItemStack item, final boolean bypassRestrictions, final CustomKitsConfig config) {
        if (item == null || item.getType() == Material.AIR) {
            return ValidationResult.ok();
        }

        final Material mat = item.getType();
        final String matName = mat.name();

        if (!bypassRestrictions) {
            if (config.getMaterialMode() == CustomKitsConfig.MaterialMode.BLOCKLIST) {
                if (config.getBlockedMaterials().contains(matName)) {
                    return ValidationResult.fail("ERROR.customkits.material-blocked", "material", matName);
                }
            } else {
                if (!config.getAllowedMaterials().contains(matName)) {
                    return ValidationResult.fail("ERROR.customkits.material-not-allowed", "material", matName);
                }
            }
        }

        if (item.getAmount() < 1 || item.getAmount() > 64) {
            return ValidationResult.fail("ERROR.customkits.invalid-amount");
        }

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return ValidationResult.ok();
        }

        // Validate Enchantments
        if (!bypassRestrictions && config.isEnchantmentsEnabled()) {
            final Map<Enchantment, Integer> enchants = item.getEnchantments();
            for (final Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                final Enchantment ench = entry.getKey();
                final int level = entry.getValue();
                final String enchName = ench.getKey().getKey().toUpperCase();

                if (config.getBlockedEnchantments().contains(enchName)) {
                    return ValidationResult.fail("ERROR.customkits.enchantment-blocked", "enchantment", enchName);
                }

                if (!config.isAllowIncompatibleEnchants() && !ench.canEnchantItem(item)) {
                    return ValidationResult.fail("ERROR.customkits.enchantment-incompatible",
                            "enchantment", enchName, "item", item.getType().name());
                }

                int maxAllowed = config.getEnchantOverrides().getOrDefault(enchName, config.getDefaultMaxEnchantLevel());
                if (!config.isAllowUnsafeEnchants() && level > maxAllowed) {
                    return ValidationResult.fail("ERROR.customkits.enchantment-level-too-high",
                            "enchantment", enchName, "max", maxAllowed);
                }
            }
        }

        // Validate Attributes
        if (!bypassRestrictions && config.isAttributesEnabled() && meta.hasAttributeModifiers()) {
            final Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
            if (modifiers != null) {
                if (modifiers.size() > config.getMaxModifiersPerItem()) {
                    return ValidationResult.fail("ERROR.customkits.too-many-attributes", "max", config.getMaxModifiersPerItem());
                }

                for (final Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
                    final Attribute attr = entry.getKey();
                    final AttributeModifier mod = entry.getValue();
                    final String attrName = attr.name();

                    final CustomKitsConfig.AttributeLimit limit = config.getAttributeLimits().get(attrName);
                    if (limit != null) {
                        if (mod.getAmount() < limit.getMin() || mod.getAmount() > limit.getMax()) {
                            return ValidationResult.fail("ERROR.customkits.attribute-amount-invalid",
                                    "attribute", attrName, "min", limit.getMin(), "max", limit.getMax());
                        }
                    }
                }
            }
        }

        return ValidationResult.ok();
    }

    public ValidationResult validateKit(final CustomKit kit, final Player player, final CustomKitsConfig config) {
        if (kit == null) {
            return ValidationResult.fail("ERROR.customkits.kit-null");
        }

        final ValidationResult nameRes = validateName(kit.getName(), kit.getOwner(), kit.getUniqueId(), config);
        if (!nameRes.isValid()) {
            return nameRes;
        }

        final boolean bypass = player != null && (
                player.hasPermission(Permissions.CUSTOMKITS_BYPASS_RESTRICTIONS) ||
                player.hasPermission(Permissions.CUSTOMKITS_ADMIN) ||
                player.hasPermission(Permissions.ADMIN)
        );

        for (final ItemStack item : kit.getItems().values()) {
            final ValidationResult res = validateItem(item, bypass, config);
            if (!res.isValid()) return res;
        }

        for (final Map.Entry<Integer, ItemStack> entry : kit.getArmor().entrySet()) {
            final int slot = entry.getKey();
            final ItemStack armorItem = entry.getValue();
            if (armorItem != null && armorItem.getType() != Material.AIR) {
                if (!isValidArmorForSlot(armorItem.getType(), slot)) {
                    return ValidationResult.fail("ERROR.customkits.invalid-armor-slot",
                            "material", armorItem.getType().name(),
                            "slot", getArmorSlotName(slot));
                }
            }
            final ValidationResult res = validateItem(armorItem, bypass, config);
            if (!res.isValid()) return res;
        }

        if (kit.getOffHand() != null) {
            final ValidationResult res = validateItem(kit.getOffHand(), bypass, config);
            if (!res.isValid()) return res;
        }

        return ValidationResult.ok();
    }
}
