package me.realized.duels.util;

import me.realized.duels.util.compat.CompatUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class PlayerUtil {

    private PlayerUtil() {}

    public static void reset(final Player player) {
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        setMaxHealth(player);
        player.setFoodLevel(20);
        player.setItemOnCursor(null);

        final Inventory top = player.getOpenInventory().getTopInventory();

        if (top != null && top.getType() == InventoryType.CRAFTING) {
            top.clear();
        }

        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().clear();
        player.updateInventory();
    }

    private static void setMaxHealth(final Player player) {
        if (CompatUtil.isPre1_9()) {
            player.setHealth(player.getMaxHealth());
        } else {
            final AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

            if (attribute == null) {
                player.setHealth(20.0D);
                return;
            }

            final double maxHealth = attribute.getValue();

            if (maxHealth == 0.0D) {
                player.setHealth(attribute.getDefaultValue());
                return;
            }

            player.setHealth(maxHealth);
        }
    }
}
