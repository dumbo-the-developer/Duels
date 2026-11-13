package com.meteordevelopments.duels.util;

import com.meteordevelopments.duels.util.compat.CompatUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PlayerUtil {

    private static final double DEFAULT_MAX_HEALTH = 20.0D;
    private static final float DEFAULT_EXHAUSTION = 0.0F;
    private static final float DEFAULT_SATURATION = 5.0F;
    private static final int DEFAULT_MAX_FOOD_LEVEL = 20;

    private PlayerUtil() {
    }

    public static double getMaxHealth(final Player player) {
        if (CompatUtil.isPre1_9()) {
            return player.getMaxHealth();
        } else {
            final AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

            if (attribute == null) {
                return DEFAULT_MAX_HEALTH;
            }

            return attribute.getValue();
        }
    }

    private static void setMaxHealth(final Player player) {
        player.setHealth(getMaxHealth(player));
    }

    public static void clearAllEffects(final Player player) {
        if (player == null) {
            return;
        }

        try {
            for (final PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
                player.removePotionEffect(effect.getType());
            }
        } catch (Throwable ignored) {
        }

        try {
            player.clearActivePotionEffects();
        } catch (Throwable ignored) {
        }

        removeOmenEffects(player);
    }

    public static void removeOmenEffects(final Player player) {
        if (player == null) {
            return;
        }

        final List<String> omenKeywords = Arrays.asList("BAD_OMEN", "RAID_OMEN", "TRIAL_OMEN", "INFESTED", "OOZING", "WEAVING", "WIND_CHARGED");
        for (final String omenName : omenKeywords) {
            try {
                final PotionEffectType type = PotionEffectType.getByName(omenName);
                if (type != null) {
                    player.removePotionEffect(type);
                }
            } catch (Throwable ignored) {
            }
            try {
                final org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(omenName.toLowerCase());
                final PotionEffectType type = org.bukkit.Registry.POTION_EFFECT_TYPE.get(key);
                if (type != null) {
                    player.removePotionEffect(type);
                }
            } catch (Throwable ignored) {
            }
        }

        // Active effects scan by name/key
        try {
            for (final PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
                if (effect != null && effect.getType() != null) {
                    final String typeName = effect.getType().getName().toUpperCase();
                    if (typeName.contains("OMEN") || typeName.contains("INFESTED") || typeName.contains("OOZING") || typeName.contains("WEAVING") || typeName.contains("WIND_CHARGED")) {
                        player.removePotionEffect(effect.getType());
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static void reset(final Player player) {
        player.setFireTicks(0);
        clearAllEffects(player);
        setMaxHealth(player);
        player.setExhaustion(DEFAULT_EXHAUSTION);
        player.setSaturation(DEFAULT_SATURATION);
        player.setFoodLevel(DEFAULT_MAX_FOOD_LEVEL);
        player.setItemOnCursor(null);
        player.getInventory().setItemInOffHand(null);

        final Inventory top = player.getOpenInventory().getTopInventory();

        if (top.getType() == InventoryType.CRAFTING) {
            top.clear();
        }

        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().clear();
        player.updateInventory();
    }
}
