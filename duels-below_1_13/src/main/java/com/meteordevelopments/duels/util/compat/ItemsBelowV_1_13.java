package com.meteordevelopments.duels.util.compat;

import com.meteordevelopments.duels.util.compat.interfaces.ItemsCompat;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class ItemsBelowV_1_13 implements ItemsCompat {

    @Override
    public ItemStack createPane(String type) {
        Material material = matchLegacyMaterial(type);
        if (material == null) {
            System.out.println("Debug: Invalid material type for pane (below 1.20.6): " + type);
            throw new IllegalArgumentException("Invalid material type: " + type);
        }
        return ItemBuilder.of(material).name(" ").build();
    }

    @Override
    public ItemStack createItem(String type) {
        Material material = matchLegacyMaterial(type);
        if (material == null) {
            System.out.println("Debug: Invalid material type (below 1.20.6): " + type);
            throw new IllegalArgumentException("Invalid material type: " + type);
        }
        return ItemBuilder.of(material).build();
    }

    @Override
    public ItemStack createPotion(Material material, PotionType type, boolean extended, boolean upgraded) {
        // For older versions, some potions might not exist or behave differently
        // In earlier versions, potions were handled differently, so we adjust here.
        return ItemBuilder.of(material)
                .potion(type, extended, upgraded)
                .build();
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
        Material material = matchLegacyMaterial(type);
        if (material == null) {
            throw new IllegalArgumentException("Invalid material type (below 1.20.6): " + type);
        }
        ItemStack item = new ItemStack(material);
        item.setDurability(data);  // Older versions use durability for variant types
        return item;
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
        return PotionType.INSTANT_HEAL;
    }

    /**
     * Helper method to match materials for older versions.
     */
    private Material matchLegacyMaterial(String type) {
        switch (type) {
            // Stained Glass Pane
            case "ORANGE_STAINED_GLASS_PANE":
            case "LIME_STAINED_GLASS_PANE":
            case "WHITE_STAINED_GLASS_PANE":
            case "MAGENTA_STAINED_GLASS_PANE":
            case "LIGHT_BLUE_STAINED_GLASS_PANE":
            case "YELLOW_STAINED_GLASS_PANE":
            case "BLUE_STAINED_GLASS_PANE":
            case "PURPLE_STAINED_GLASS_PANE":
            case "PINK_STAINED_GLASS_PANE":
            case "RED_STAINED_GLASS_PANE":
            case "BLACK_STAINED_GLASS_PANE":
            case "GRAY_STAINED_GLASS_PANE":
            case "GREEN_STAINED_GLASS_PANE":
            case "CYAN_STAINED_GLASS_PANE":
                return Material.STAINED_GLASS_PANE;  // Pre-1.13 Material for stained glass pane

            // Glass
            case "ORANGE_GLASS":
            case "LIME_GLASS":
            case "WHITE_GLASS":
            case "MAGENTA_GLASS":
            case "LIGHT_BLUE_GLASS":
            case "YELLOW_GLASS":
            case "BLUE_GLASS":
            case "PURPLE_GLASS":
            case "PINK_GLASS":
            case "RED_GLASS":
            case "BLACK_GLASS":
            case "GRAY_GLASS":
            case "GREEN_GLASS":
            case "CYAN_GLASS":
                return Material.GLASS; // Legacy glass material

            // Skull types
            case "PLAYER_HEAD":
                return Material.SKULL_ITEM; // For player head
            case "SKELETON_SKULL":
                return Material.SKULL; // For skeleton skull

            // Dyes
            case "GRAY_DYE":
            case "LIME_DYE":
            case "MAGENTA_DYE":
            case "LIGHT_BLUE_DYE":
            case "YELLOW_DYE":
            case "BLUE_DYE":
            case "PURPLE_DYE":
            case "PINK_DYE":
            case "RED_DYE":
            case "BLACK_DYE":
            case "WHITE_DYE":
            case "BROWN_DYE":
            case "GREEN_DYE":
            case "CYAN_DYE":
                return Material.INK_SACK;  // Legacy dye material
            case "MUSHROOM_STEW":
                return Material.MUSHROOM_SOUP;
            // Signs
            case "OAK_SIGN":
            case "BIRCH_SIGN":
            case "SPRUCE_SIGN":
            case "JUNGLE_SIGN":
            case "ACACIA_SIGN":
            case "DARK_OAK_SIGN":
            case "CRIMSON_SIGN":
            case "WARPED_SIGN":
                return Material.SIGN; // All sign types fall back to legacy sign

            // Planks and Woods
            case "OAK_PLANKS":
            case "BIRCH_PLANKS":
            case "SPRUCE_PLANKS":
            case "JUNGLE_PLANKS":
            case "ACACIA_PLANKS":
            case "DARK_OAK_PLANKS":
            case "CRIMSON_PLANKS":
            case "WARPED_PLANKS":
                return Material.WOOD; // Legacy wood material

            case "OAK_WOOD":
            case "BIRCH_WOOD":
            case "SPRUCE_WOOD":
            case "JUNGLE_WOOD":
            case "ACACIA_WOOD":
            case "DARK_OAK_WOOD":
            case "CRIMSON_STEM":
            case "WARPED_STEM":
                return Material.WOOD;

            case "ENCHANTED_GOLDEN_APPLE":
                return Material.GOLDEN_APPLE;

            default:
                return Material.matchMaterial(type);
        }
    }
}


