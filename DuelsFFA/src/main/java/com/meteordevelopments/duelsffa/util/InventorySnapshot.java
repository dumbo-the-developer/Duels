package com.meteordevelopments.duelsffa.util;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventorySnapshot {

    private final ItemStack[] contents;
    private final ItemStack[] armor;
    private final ItemStack offhand;
    private final int foodLevel;
    private final float saturation;
    private final float exhaustion;
    private final int level;
    private final float exp;
    private final double health;
    private final GameMode gameMode;
    private final boolean allowFlight;
    private final boolean flying;

    private InventorySnapshot(ItemStack[] contents, ItemStack[] armor, ItemStack offhand, int foodLevel,
                              float saturation, float exhaustion, int level, float exp, double health,
                              GameMode gameMode, boolean allowFlight, boolean flying) {
        this.contents = contents;
        this.armor = armor;
        this.offhand = offhand;
        this.foodLevel = foodLevel;
        this.saturation = saturation;
        this.exhaustion = exhaustion;
        this.level = level;
        this.exp = exp;
        this.health = health;
        this.gameMode = gameMode;
        this.allowFlight = allowFlight;
        this.flying = flying;
    }

    public static InventorySnapshot capture(final Player player) {
        PlayerInventory inv = player.getInventory();
        return new InventorySnapshot(
                clone(inv.getContents()),
                clone(inv.getArmorContents()),
                inv.getItemInOffHand() != null ? inv.getItemInOffHand().clone() : null,
                player.getFoodLevel(),
                player.getSaturation(),
                player.getExhaustion(),
                player.getLevel(),
                player.getExp(),
                player.getHealth(),
                player.getGameMode(),
                player.getAllowFlight(),
                player.isFlying()
        );
    }

    public void apply(final Player player) {
        PlayerInventory inv = player.getInventory();
        inv.setContents(clone(contents));
        inv.setArmorContents(clone(armor));
        inv.setItemInOffHand(offhand != null ? offhand.clone() : null);
        player.setFoodLevel(foodLevel);
        player.setSaturation(saturation);
        player.setExhaustion(exhaustion);
        player.setLevel(level);
        player.setExp(exp);
        player.setGameMode(gameMode);
        player.setAllowFlight(allowFlight);
        player.setFlying(flying);
        double maxHealth = player.getMaxHealth();
        player.setHealth(Math.min(health, maxHealth));
        player.updateInventory();
    }

    private static ItemStack[] clone(ItemStack[] items) {
        if (items == null) return null;
        ItemStack[] copy = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            copy[i] = items[i] != null ? items[i].clone() : null;
        }
        return copy;
    }
}
