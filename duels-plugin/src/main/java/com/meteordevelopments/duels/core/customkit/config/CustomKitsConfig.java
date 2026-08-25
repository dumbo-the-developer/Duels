package com.meteordevelopments.duels.core.customkit.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

@Getter
public class CustomKitsConfig {

    public enum CloseBehavior {
        CONFIRM,
        DISCARD,
        AUTO_SAVE_DRAFT
    }

    public enum MaterialMode {
        BLOCKLIST,
        ALLOWLIST
    }

    @Getter
    public static class AttributeLimit {
        private final double min;
        private final double max;

        public AttributeLimit(final double min, final double max) {
            this.min = min;
            this.max = max;
        }
    }

    private boolean enabled = true;


    private int defaultLimit = 1;
    private String unlimitedPermission = "duels.customkits.limit.unlimited";
    private final Map<String, Integer> permissionLimits = new HashMap<>();


    private int minNameLength = 3;
    private int maxNameLength = 24;
    private boolean allowSpaces = true;
    private String allowedPattern = "[a-zA-Z0-9 _-]+";


    private CloseBehavior closeBehavior = CloseBehavior.CONFIRM;


    private MaterialMode materialMode = MaterialMode.BLOCKLIST;
    private final Set<String> blockedMaterials = new HashSet<>();
    private final Set<String> allowedMaterials = new HashSet<>();


    private boolean enchantmentsEnabled = true;
    private boolean allowUnsafeEnchants = false;
    private boolean allowIncompatibleEnchants = false;
    private int defaultMaxEnchantLevel = 5;
    private final Set<String> blockedEnchantments = new HashSet<>();
    private final Map<String, Integer> enchantOverrides = new HashMap<>();


    private boolean attributesEnabled = true;
    private int maxModifiersPerItem = 5;
    private final Map<String, AttributeLimit> attributeLimits = new HashMap<>();


    private double minBet = 0.0;
    private double maxBet = 1000000.0;
    private boolean allowZeroBet = true;


    private boolean miniMessageEnabled = true;

    public void load(final FileConfiguration config) {
        final ConfigurationSection sec = config.getConfigurationSection("custom-kits");
        if (sec == null) {
            initDefaults();
            return;
        }

        this.enabled = sec.getBoolean("enabled", true);

        // Limits
        final ConfigurationSection limitsSec = sec.getConfigurationSection("limits");
        if (limitsSec != null) {
            this.defaultLimit = limitsSec.getInt("default", 1);
            this.unlimitedPermission = limitsSec.getString("unlimited-permission", "duels.customkits.limit.unlimited");

            permissionLimits.clear();
            final ConfigurationSection permsSec = limitsSec.getConfigurationSection("permissions");
            if (permsSec != null) {
                for (final String key : permsSec.getKeys(false)) {
                    permissionLimits.put(key, permsSec.getInt(key));
                }
            } else {
                permissionLimits.put("duels.customkits.limit.1", 1);
                permissionLimits.put("duels.customkits.limit.3", 3);
                permissionLimits.put("duels.customkits.limit.5", 5);
                permissionLimits.put("duels.customkits.limit.10", 10);
            }
        }


        final ConfigurationSection namingSec = sec.getConfigurationSection("naming");
        if (namingSec != null) {
            this.minNameLength = namingSec.getInt("min-length", 3);
            this.maxNameLength = namingSec.getInt("max-length", 24);
            this.allowSpaces = namingSec.getBoolean("allow-spaces", true);
            this.allowedPattern = namingSec.getString("allowed-pattern", "[a-zA-Z0-9 _-]+");
        }


        final ConfigurationSection editingSec = sec.getConfigurationSection("editing");
        if (editingSec != null) {
            final String closeStr = editingSec.getString("close-behavior", "CONFIRM");
            try {
                this.closeBehavior = CloseBehavior.valueOf(closeStr.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                this.closeBehavior = CloseBehavior.CONFIRM;
            }
        }


        final ConfigurationSection restrictionsSec = sec.getConfigurationSection("restrictions");
        if (restrictionsSec != null) {
            final String modeStr = restrictionsSec.getString("material-mode", "BLOCKLIST");
            try {
                this.materialMode = MaterialMode.valueOf(modeStr.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                this.materialMode = MaterialMode.BLOCKLIST;
            }

            blockedMaterials.clear();
            for (final String mat : restrictionsSec.getStringList("blocked-materials")) {
                blockedMaterials.add(mat.toUpperCase(Locale.ROOT));
            }

            allowedMaterials.clear();
            for (final String mat : restrictionsSec.getStringList("allowed-materials")) {
                allowedMaterials.add(mat.toUpperCase(Locale.ROOT));
            }
        }


        final ConfigurationSection enchSec = sec.getConfigurationSection("enchantments");
        if (enchSec != null) {
            this.enchantmentsEnabled = enchSec.getBoolean("enabled", true);
            this.allowUnsafeEnchants = enchSec.getBoolean("allow-unsafe", false);
            this.allowIncompatibleEnchants = enchSec.getBoolean("allow-incompatible", false);
            this.defaultMaxEnchantLevel = enchSec.getInt("default-max-level", 5);

            blockedEnchantments.clear();
            for (final String ench : enchSec.getStringList("blocked")) {
                blockedEnchantments.add(ench.toUpperCase(Locale.ROOT));
            }

            enchantOverrides.clear();
            final ConfigurationSection overridesSec = enchSec.getConfigurationSection("overrides");
            if (overridesSec != null) {
                for (final String ench : overridesSec.getKeys(false)) {
                    enchantOverrides.put(ench.toUpperCase(Locale.ROOT), overridesSec.getInt(ench));
                }
            }
        }


        final ConfigurationSection attrSec = sec.getConfigurationSection("attributes");
        if (attrSec != null) {
            this.attributesEnabled = attrSec.getBoolean("enabled", true);
            this.maxModifiersPerItem = attrSec.getInt("max-modifiers-per-item", 5);

            attributeLimits.clear();
            final ConfigurationSection attrLimitsSec = attrSec.getConfigurationSection("limits");
            if (attrLimitsSec != null) {
                for (final String attr : attrLimitsSec.getKeys(false)) {
                    final double min = attrLimitsSec.getDouble(attr + ".min", -100.0);
                    final double max = attrLimitsSec.getDouble(attr + ".max", 100.0);
                    attributeLimits.put(attr.toUpperCase(Locale.ROOT), new AttributeLimit(min, max));
                }
            }
        }


        final ConfigurationSection bettingSec = sec.getConfigurationSection("betting");
        if (bettingSec != null) {
            this.minBet = bettingSec.getDouble("minimum", 0.0);
            this.maxBet = bettingSec.getDouble("maximum", 1000000.0);
            this.allowZeroBet = bettingSec.getBoolean("allow-zero", true);
        }


        final ConfigurationSection formattingSec = sec.getConfigurationSection("formatting");
        if (formattingSec != null) {
            this.miniMessageEnabled = formattingSec.getBoolean("minimessage", true);
        }
    }

    private void initDefaults() {
        permissionLimits.put("duels.customkits.limit.1", 1);
        permissionLimits.put("duels.customkits.limit.3", 3);
        permissionLimits.put("duels.customkits.limit.5", 5);
        permissionLimits.put("duels.customkits.limit.10", 10);

        blockedMaterials.addAll(Arrays.asList(
                "BEDROCK", "BARRIER", "STRUCTURE_BLOCK", "STRUCTURE_VOID",
                "COMMAND_BLOCK", "CHAIN_COMMAND_BLOCK", "REPEATING_COMMAND_BLOCK",
                "COMMAND_BLOCK_MINECART", "JIGSAW", "LIGHT"
        ));

        blockedEnchantments.add("MENDING");
        attributeLimits.put("GENERIC_ATTACK_DAMAGE", new AttributeLimit(-10, 20));
        attributeLimits.put("GENERIC_ATTACK_SPEED", new AttributeLimit(-4, 20));
        attributeLimits.put("GENERIC_ARMOR", new AttributeLimit(0, 30));
        attributeLimits.put("GENERIC_ARMOR_TOUGHNESS", new AttributeLimit(0, 20));
        attributeLimits.put("GENERIC_KNOCKBACK_RESISTANCE", new AttributeLimit(0, 1));
        attributeLimits.put("GENERIC_MOVEMENT_SPEED", new AttributeLimit(-0.5, 1.0));
        attributeLimits.put("GENERIC_MAX_HEALTH", new AttributeLimit(1, 40));
    }
}
