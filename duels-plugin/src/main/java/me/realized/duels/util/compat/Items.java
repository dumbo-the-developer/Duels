package me.realized.duels.util.compat;

import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public final class Items {

    private static final String PANE = "STAINED_GLASS_PANE";

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
        ORANGE_PANE = (CompatUtil.isPre1_13() ? ItemBuilder.of(PANE, 1, (short) 1) : ItemBuilder.of(Material.ORANGE_STAINED_GLASS_PANE)).name(" ").build();
        BLUE_PANE = (CompatUtil.isPre1_13() ? ItemBuilder.of(PANE, 1, (short) 11) : ItemBuilder.of(Material.BLUE_STAINED_GLASS_PANE)).name(" ").build();
        RED_PANE = (CompatUtil.isPre1_13() ? ItemBuilder.of(PANE, 1, (short) 14) : ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)).build();
        GRAY_PANE = (CompatUtil.isPre1_13() ? ItemBuilder.of(PANE, 1, (short) 7) : ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)).name(" ").build();
        WHITE_PANE = (CompatUtil.isPre1_13() ? ItemBuilder.of(PANE, 1, (short) 0) : ItemBuilder.of(Material.WHITE_STAINED_GLASS_PANE)).name(" ").build();
        GREEN_PANE = (CompatUtil.isPre1_13() ? ItemBuilder.of(PANE, 1, (short) 5) : ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE)).build();
        HEAD = (CompatUtil.isPre1_13() ? ItemBuilder.of("SKULL_ITEM", 1, (short) 3) : ItemBuilder.of(Material.PLAYER_HEAD)).build();
        SKELETON_HEAD = CompatUtil.isPre1_13() ? Material.matchMaterial("SKULL_ITEM") : Material.SKELETON_SKULL;
        OFF = (CompatUtil.isPre1_13() ? ItemBuilder.of("INK_SACK", 1, (short) 8) : ItemBuilder.of(Material.GRAY_DYE)).build();
        ON = (CompatUtil.isPre1_13() ? ItemBuilder.of("INK_SACK", 1, (short) 10) : ItemBuilder.of(Material.LIME_DYE)).build();
        MUSHROOM_SOUP = CompatUtil.isPre1_13() ? Material.matchMaterial("MUSHROOM_SOUP") : Material.MUSHROOM_STEW;
        EMPTY_MAP = CompatUtil.isPre1_13() ? Material.matchMaterial("EMPTY_MAP") : Material.MAP;
        SIGN = CompatUtil.isPre1_14() ? Material.matchMaterial("SIGN") : Material.OAK_SIGN;
        HEAL_SPLASH_POTION = (CompatUtil.isPre1_9() ? ItemBuilder.of(Material.POTION, 1, (short) 16421) : ItemBuilder.of(Material.SPLASH_POTION).potion(
            PotionType.INSTANT_HEAL, false, true)).build();
        WATER_BREATHING_POTION = (CompatUtil.isPre1_9() ? ItemBuilder.of(Material.POTION, 1, (short) 8237) : ItemBuilder.of(Material.POTION).potion(
            PotionType.WATER_BREATHING, false, false)).build();
        ENCHANTED_GOLDEN_APPLE = CompatUtil.isPre1_13() ?
            ItemBuilder.of(Material.GOLDEN_APPLE, 1, (short) 1).build() : ItemBuilder.of(Material.ENCHANTED_GOLDEN_APPLE).build();
    }

    public static boolean equals(final ItemStack item, final ItemStack other) {
        return item.getType() == other.getType() && getDurability(item) == getDurability(other);
    }

    public static ItemStack from(final String type, final short data) {
        if (type.equalsIgnoreCase("STAINED_GLASS_PANE") && !CompatUtil.isPre1_13()) {
            return ItemBuilder.of(Panes.from(data)).name(" ").build();
        }

        return ItemBuilder.of(type, 1, data).name(" ").build();
    }

    public static short getDurability(final ItemStack item) {
        if (CompatUtil.isPre1_13()) {
            return item.getDurability();
        }

        final ItemMeta meta;
        return ((meta = item.getItemMeta()) == null) ? 0 : (short) ((Damageable) meta).getDamage();
    }

    public static void setDurability(final ItemStack item, final short durability) {
        if (CompatUtil.isPre1_13()) {
            item.setDurability(durability);
            return;
        }

        final ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            ((Damageable) meta).setDamage(durability);
            item.setItemMeta(meta);
        }
    }

    public static boolean isHealSplash(final ItemStack item) {
        if (CompatUtil.isPre1_9()) {
            return Items.equals(Items.HEAL_SPLASH_POTION, item);
        }

        if (item.getType() != Material.SPLASH_POTION) {
            return false;
        }

        final PotionMeta meta = (PotionMeta) item.getItemMeta();
        return meta != null && meta.getBasePotionData().getType() == PotionType.INSTANT_HEAL;
    }

    private Items() {}
}
