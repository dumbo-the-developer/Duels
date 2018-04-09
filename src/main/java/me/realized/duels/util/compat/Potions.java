package me.realized.duels.util.compat;

import java.lang.reflect.Method;
import java.util.Collection;
import me.realized.duels.util.ReflectionUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

public class Potions {

    private static final Method AS_NMS_COPY;
    private static final Class<?> TAG_COMPOUND;
    private static final Method SET_STRING;
    private static final Method GET_TAG;
    private static final Method SET_TAG;
    private static final Method AS_BUKKIT_COPY;

    static {
        final Class<?> CB_ITEMSTACK;
        AS_NMS_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK = ReflectionUtil.getCBClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class);
        TAG_COMPOUND = ReflectionUtil.getNMSClass("NBTTagCompound");
        SET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "setString", String.class, String.class);
        final Class<?> NMS_ITEMSTACK;
        GET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK = ReflectionUtil.getNMSClass("ItemStack"), "getTag");
        SET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "setTag", TAG_COMPOUND);
        AS_BUKKIT_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asBukkitCopy", NMS_ITEMSTACK);
    }

    private final PotionType type;
    private final boolean strong, extended, linger, splash;

    public Potions(PotionType type, Collection<String> args) {
        this.type = type;
        this.strong = args.contains("strong");
        this.extended = args.contains("extended");
        this.linger = args.contains("linger");
        this.splash = args.contains("splash");
    }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(Material.POTION);

        if (splash) {
            item = new ItemStack(Material.SPLASH_POTION);
        } else if (linger) {
            item = new ItemStack(Material.LINGERING_POTION);
        }

        try {
            Object stack = AS_NMS_COPY.invoke(null, item);
            Object tagCompound = GET_TAG.invoke(stack);

            if (tagCompound == null) {
                tagCompound = TAG_COMPOUND.newInstance();
            }

            String tag;

            switch (type) {
                case UNCRAFTABLE:
                    tag = "empty";
                    break;
                case WATER:
                    tag = "water";
                    break;
                case MUNDANE:
                    tag = "mundane";
                    break;
                case THICK:
                    tag = "thick";
                    break;
                case AWKWARD:
                    tag = "awkward";
                    break;
                case NIGHT_VISION:
                    if (extended) {
                        tag = "long_night_vision";
                    } else {
                        tag = "night_vision";
                    }
                    break;
                case INVISIBILITY:
                    if (extended) {
                        tag = "long_invisibility";
                    } else {
                        tag = "invisibility";
                    }
                    break;
                case JUMP:
                    if (extended) {
                        tag = "long_leaping";
                    } else if (strong) {
                        tag = "strong_leaping";
                    } else {
                        tag = "leaping";
                    }
                    break;
                case FIRE_RESISTANCE:
                    if (extended) {
                        tag = "long_fire_resistance";
                    } else {
                        tag = "fire_resistance";
                    }
                    break;
                case SPEED:
                    if (extended) {
                        tag = "long_swiftness";
                    } else if (strong) {
                        tag = "strong_swiftness";
                    } else {
                        tag = "swiftness";
                    }
                    break;
                case SLOWNESS:
                    if (extended) {
                        tag = "long_slowness";
                    } else {
                        tag = "slowness";
                    }
                    break;
                case WATER_BREATHING:
                    if (extended) {
                        tag = "long_water_breathing";
                    } else {
                        tag = "water_breathing";
                    }
                    break;
                case INSTANT_HEAL:
                    if (strong) {
                        tag = "strong_healing";
                    } else {
                        tag = "healing";
                    }
                    break;
                case INSTANT_DAMAGE:
                    if (strong) {
                        tag = "strong_harming";
                    } else {
                        tag = "harming";
                    }
                    break;
                case POISON:
                    if (extended) {
                        tag = "long_poison";
                    } else if (strong) {
                        tag = "strong_poison";
                    } else {
                        tag = "poison";
                    }
                    break;
                case REGEN:
                    if (extended) {
                        tag = "long_regeneration";
                    } else if (strong) {
                        tag = "strong_regeneration";
                    } else {
                        tag = "regeneration";
                    }
                    break;
                case STRENGTH:
                    if (extended) {
                        tag = "long_strength";
                    } else if (strong) {
                        tag = "strong_strength";
                    } else {
                        tag = "strength";
                    }
                    break;
                case WEAKNESS:
                    if (extended) {
                        tag = "long_weakness";
                    } else {
                        tag = "weakness";
                    }
                    break;
                case LUCK:
                    tag = "luck";
                    break;
                default:
                    return null;
            }

            SET_STRING.invoke(tagCompound, "Potion", "minecraft:" + tag);
            SET_TAG.invoke(stack, tagCompound);
            return (ItemStack) AS_BUKKIT_COPY.invoke(null, stack);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
