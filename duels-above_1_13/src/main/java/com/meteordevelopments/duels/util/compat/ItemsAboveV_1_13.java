package com.meteordevelopments.duels.util.compat;

import com.meteordevelopments.duels.util.compat.interfaces.ItemsCompat;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class ItemsAboveV_1_13 implements ItemsCompat {

    @Override
    public ItemStack createPane(String type) {
        Material material = Material.matchMaterial(type);
        if (material == null) {
            System.out.println("Debug: Invalid material type for pane: " + type);
            throw new IllegalArgumentException("Invalid material type: " + type);
        }
        return ItemBuilder.of(material).name(" ").build();
    }

    @Override
    public ItemStack createItem(String type) {
        Material material = Material.matchMaterial(type);
        if (material == null) {
            System.out.println("Debug: Invalid material type: " + type);
            throw new IllegalArgumentException("Invalid material type: " + type);
        }
        return ItemBuilder.of(material).build();
    }

    @Override
    public ItemStack createPotion(Material material, PotionType type, boolean extended, boolean upgraded) {
        return ItemBuilder.of(material).potion(type, extended, upgraded).build();
    }

    @Override
    public boolean itemEquals(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        if (item1.getType() != item2.getType()) {
            return false;
        }
        if (item1.hasItemMeta() && item2.hasItemMeta()) {
            return item1.getItemMeta().equals(item2.getItemMeta());
        }
        return !item1.hasItemMeta() && !item2.hasItemMeta();
    }

    @Override
    public ItemStack from(String type, short data) {
        Material material = Material.matchMaterial(type);
        if (material == null) {
            throw new IllegalArgumentException("Invalid material type: " + type);
        }
        return ItemBuilder.of(material).build();
    }

    @Override
    public boolean isHealSplash(ItemStack item) {
        if (item == null || item.getType() != Material.SPLASH_POTION) {
            return false;
        }
        if (!(item.getItemMeta() instanceof PotionMeta)) {
            return false;
        }
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        return meta.getBasePotionData().getType() == getHealingPotionType();
    }

    @Override
    public PotionType getHealingPotionType() {
        return PotionType.INSTANT_HEAL;  // Minecraft 1.20.6 and above uses HEALING
    }
}

