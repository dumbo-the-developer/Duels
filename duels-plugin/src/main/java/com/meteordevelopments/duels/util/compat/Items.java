package com.meteordevelopments.duels.util.compat;

import com.meteordevelopments.duels.util.compat.interfaces.ItemsCompat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

public final class Items {

    private static final ItemsCompat ITEMS_COMPAT = ItemsFactory.getItemsCompat();

    public static final ItemStack ORANGE_PANE;
    public static final ItemStack BLUE_PANE;
    public static final ItemStack RED_PANE;
    public static final ItemStack GRAY_PANE;
    public static final ItemStack WHITE_PANE;
    public static final ItemStack GREEN_PANE;
    public static final ItemStack HEAD;
    public static final Material SKELETON_HEAD;
    public static final ItemStack OFF;
    public static final ItemStack ON;
    public static final Material MUSHROOM_SOUP;
    public static final Material EMPTY_MAP;
    public static final Material SIGN;
    public static final ItemStack HEAL_SPLASH_POTION;
    public static final ItemStack WATER_BREATHING_POTION;
    public static final ItemStack ENCHANTED_GOLDEN_APPLE;

    static {
        ORANGE_PANE = ITEMS_COMPAT.createPane("ORANGE_STAINED_GLASS_PANE");
        BLUE_PANE = ITEMS_COMPAT.createPane("BLUE_STAINED_GLASS_PANE");
        RED_PANE = ITEMS_COMPAT.createPane("RED_STAINED_GLASS_PANE");
        GRAY_PANE = ITEMS_COMPAT.createPane("GRAY_STAINED_GLASS_PANE");
        WHITE_PANE = ITEMS_COMPAT.createPane("WHITE_STAINED_GLASS_PANE");
        GREEN_PANE = ITEMS_COMPAT.createPane("LIME_STAINED_GLASS_PANE");
        HEAD = ITEMS_COMPAT.createItem("PLAYER_HEAD");
        SKELETON_HEAD = ITEMS_COMPAT.createItem("SKELETON_SKULL").getType();
        OFF = ITEMS_COMPAT.createItem("GRAY_DYE");
        ON = ITEMS_COMPAT.createItem("LIME_DYE");
        MUSHROOM_SOUP = ITEMS_COMPAT.createItem("MUSHROOM_STEW").getType();
        EMPTY_MAP = Material.MAP;
        SIGN = ITEMS_COMPAT.createItem("OAK_SIGN").getType();
        if (!ItemsFactory.isCurrentVersionEqualOrAbove("1.9") || !ItemsFactory.isCurrentVersionEqualOrAbove("1.13")) {
            HEAL_SPLASH_POTION = ITEMS_COMPAT.createPotion(null, null, false, true);
        }else{
            HEAL_SPLASH_POTION = ITEMS_COMPAT.createPotion(Material.SPLASH_POTION, ITEMS_COMPAT.getHealingPotionType(), false, true);
        }
        WATER_BREATHING_POTION = ITEMS_COMPAT.createPotion(Material.POTION, PotionType.WATER_BREATHING, false, false);
        ENCHANTED_GOLDEN_APPLE = ITEMS_COMPAT.createItem("ENCHANTED_GOLDEN_APPLE");
    }

    private Items() {}

    public static boolean itemEquals(ItemStack item1, ItemStack item2) {
        return ITEMS_COMPAT.itemEquals(item1, item2);
    }

    public static ItemStack from(String type, short data) {
        return ITEMS_COMPAT.from(type, data);
    }

    public static boolean isHealSplash(ItemStack item) {
        return ITEMS_COMPAT.isHealSplash(item);
    }
}
