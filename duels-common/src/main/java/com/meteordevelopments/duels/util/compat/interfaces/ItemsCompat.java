package com.meteordevelopments.duels.util.compat.interfaces;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

public interface ItemsCompat {
    ItemStack createPane(String type);
    ItemStack createItem(String type);
    ItemStack createPotion(Material material, PotionType type, boolean extended, boolean upgraded);
    boolean itemEquals(ItemStack item1, ItemStack item2);
    ItemStack from(String type, short data);
    boolean isHealSplash(ItemStack item);
    PotionType getHealingPotionType();
}

