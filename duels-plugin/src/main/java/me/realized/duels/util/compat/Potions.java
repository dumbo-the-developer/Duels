package me.realized.duels.util.compat;

import java.lang.reflect.InvocationTargetException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public final class Potions extends CompatBase {

    public static String fromBukkit(final PotionData potionData) {
        try {
            return (String) FROM_BUKKIT.invoke(null, potionData);
        } catch (InvocationTargetException | IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
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

    private Potions() {}
}
