package me.realized.duels.util.compat;

import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public final class Items {

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
        ORANGE_PANE = createPane("ORANGE_STAINED_GLASS_PANE");
        BLUE_PANE = createPane("BLUE_STAINED_GLASS_PANE");
        RED_PANE = createPane("RED_STAINED_GLASS_PANE");
        GRAY_PANE = createPane("GRAY_STAINED_GLASS_PANE");
        WHITE_PANE = createPane("WHITE_STAINED_GLASS_PANE");
        GREEN_PANE = createPane("LIME_STAINED_GLASS_PANE");
        HEAD = createItem("PLAYER_HEAD");
        SKELETON_HEAD = Material.SKELETON_SKULL;
        OFF = createItem("GRAY_DYE");
        ON = createItem("LIME_DYE");
        MUSHROOM_SOUP = Material.MUSHROOM_STEW;
        EMPTY_MAP = Material.MAP;
        SIGN = Material.OAK_SIGN;
        HEAL_SPLASH_POTION = createPotion(Material.SPLASH_POTION, PotionType.INSTANT_HEAL, false, true);
        WATER_BREATHING_POTION = createPotion(Material.POTION, PotionType.WATER_BREATHING, false, false);
        ENCHANTED_GOLDEN_APPLE = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
    }

    private Items() {}

    private static ItemStack createPane(String type) {
        Material material = Material.matchMaterial(type);
        if (material == null) {
            System.out.println("Debug: Invalid material type for pane: " + type);
            throw new IllegalArgumentException("Invalid material type: " + type);
        }
        return ItemBuilder.of(material).name(" ").build();
    }

    private static ItemStack createItem(String type) {
        Material material = Material.matchMaterial(type);
        if (material == null) {
            System.out.println("Debug: Invalid material type: " + type);
            throw new IllegalArgumentException("Invalid material type: " + type);
        }
        return ItemBuilder.of(material).build();
    }

    private static ItemStack createPotion(Material material, PotionType type, boolean extended, boolean upgraded) {
        return ItemBuilder.of(material).potion(type, extended, upgraded).build();
    }

    public static boolean itemEquals(ItemStack item1, ItemStack item2) {
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

    public static ItemStack from(String type, short data) {
        Material material = Material.matchMaterial(type);
        if (material == null) {
            throw new IllegalArgumentException("Invalid material type: " + type);
        }
        return ItemBuilder.of(material).build();
    }

    public static boolean isHealSplash(ItemStack item) {
        if (item == null || item.getType() != Material.SPLASH_POTION) {
            return false;
        }
        if (!(item.getItemMeta() instanceof PotionMeta)) {
            return false;
        }
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        return meta.getBasePotionData().getType() == PotionType.INSTANT_HEAL;
    }
}