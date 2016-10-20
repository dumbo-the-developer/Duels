package me.realized.duels.utilities.compat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class Potions {

    public enum PotionType {

        FIRE_RESISTANCE, INSTANT_DAMAGE, INSTANT_HEAL,
        INVISIBILITY, JUMP, LUCK,
        NIGHT_VISION, POISON, REGEN,
        SLOWNESS, SPEED, STRENGTH,
        WATER, WATER_BREATHING, WEAKNESS,
        EMPTY, MUNDANE, THICK, AWKWARD
    }

    private static final Class<?> itemStack = CompatHelper.getMinecraftStack();
    private static final Class<?> nbtTagCompound = CompatHelper.getNBTTagCompound();
    private static final Method asNMSCopy = CompatHelper.asNMSCopy();
    private static final Method asBukkitCopy = CompatHelper.asBukkitCopy();

    private final PotionType type;
    private final boolean strong, extended, linger, splash;

    public Potions(PotionType type, boolean strong, boolean extended, boolean linger, boolean splash) {
        this.type = type;
        this.strong = strong;
        this.extended = extended;
        this.linger = linger;
        this.splash = splash;
    }

    public PotionType getType() {
        return type;
    }

    public boolean strong() {
        return strong;
    }

    public boolean extended() {
        return extended;
    }

    public boolean linger() {
        return linger;
    }

    public boolean splash() {
        return splash;
    }

    public ItemStack toItemStack(int amount) {
        ItemStack item = new ItemStack(Material.POTION, amount);

        if (splash) {
            item.setType(Material.SPLASH_POTION);
        } else if (linger) {
            item.setType(Material.LINGERING_POTION);
        }

        try {
            Object stack = asNMSCopy.invoke(null, item);
            Object tagCompound = itemStack.getMethod("getTag").invoke(stack);

            if (tagCompound == null) {
                tagCompound = nbtTagCompound.newInstance();
            }

            String tag;

            if (type.equals(PotionType.FIRE_RESISTANCE)) {
                if (extended) {
                    tag = "long_fire_resistance";
                } else {
                    tag = "fire_resistance";
                }
            } else if (type.equals(PotionType.INSTANT_DAMAGE)) {
                if (strong) {
                    tag = "strong_harming";
                } else {
                    tag = "harming";
                }
            } else if (type.equals(PotionType.INSTANT_HEAL)) {
                if (strong) {
                    tag = "strong_healing";
                } else {
                    tag = "healing";
                }
            } else if (type.equals(PotionType.INVISIBILITY)) {
                if (extended) {
                    tag = "long_invisibility";
                } else {
                    tag = "invisibility";
                }
            } else if (type.equals(PotionType.JUMP)) {
                if (extended) {
                    tag = "long_leaping";
                } else if (strong) {
                    tag = "strong_leaping";
                } else {
                    tag = "leaping";
                }
            } else if (type.equals(PotionType.LUCK)) {
                tag = "luck";
            } else if (type.equals(PotionType.NIGHT_VISION)) {
                if (extended) {
                    tag = "long_night_vision";
                } else {
                    tag = "night_vision";
                }
            } else if (type.equals(PotionType.POISON)) {
                if (extended) {
                    tag = "long_poison";
                } else if (strong) {
                    tag = "strong_poison";
                } else {
                    tag = "poison";
                }
            } else if (type.equals(PotionType.REGEN)) {
                if (extended) {
                    tag = "long_regeneration";
                } else if (strong) {
                    tag = "strong_regeneration";
                } else {
                    tag = "regeneration";
                }
            } else if (type.equals(PotionType.SLOWNESS)) {
                if (extended) {
                    tag = "long_slowness";
                } else {
                    tag = "slowness";
                }
            } else if (type.equals(PotionType.SPEED)) {
                if (extended) {
                    tag = "long_swiftness";
                } else if (strong) {
                    tag = "strong_swiftness";
                } else {
                    tag = "swiftness";
                }
            } else if (type.equals(PotionType.STRENGTH)) {
                if (extended) {
                    tag = "long_strength";
                } else if (strong) {
                    tag = "strong_strength";
                } else {
                    tag = "strength";
                }
            } else if (type.equals(PotionType.WATER_BREATHING)) {
                if (extended) {
                    tag = "long_water_breathing";
                } else {
                    tag = "water_breathing";
                }
            } else if (type.equals(PotionType.WATER)) {
                tag = "water";
            } else if (type.equals(PotionType.WEAKNESS)) {
                if (extended) {
                    tag = "long_weakness";
                } else {
                    tag = "weakness";
                }
            } else if (type.equals(PotionType.EMPTY)) {
                tag = "empty";
            } else if (type.equals(PotionType.MUNDANE)) {
                tag = "mundane";
            } else if (type.equals(PotionType.THICK)) {
                tag = "thick";
            } else if (type.equals(PotionType.AWKWARD)) {
                tag = "awkward";
            } else {
                return null;
            }

            nbtTagCompound.getMethod("setString", String.class, String.class).invoke(tagCompound, "Potion", "minecraft:" + tag);
            itemStack.getMethod("setTag", nbtTagCompound).invoke(stack, tagCompound);
            return (ItemStack) asBukkitCopy.invoke(null, stack);
        } catch (Exception e) {
            return null;
        }
    }

    public static Potions fromItemStack(ItemStack item) {
        if (item == null || !item.getType().name().contains("POTION")) {
            throw new IllegalArgumentException("item is not a potion");
        }

        try {
            Object stack = asNMSCopy.invoke(null, item);
            Object tagCompound = itemStack.getMethod("getTag").invoke(stack);
            Object potion = nbtTagCompound.getMethod("getString", String.class).invoke(tagCompound, "Potion");

            if (tagCompound != null && potion != null) {
                String tag = ((String) potion).replace("minecraft:", "");
                PotionType type;
                boolean strong = tag.contains("strong");
                boolean _long = tag.contains("long");

                switch (tag) {
                    case "fire_resistance":
                    case "long_fire_resistance":
                        type = PotionType.FIRE_RESISTANCE;
                        break;
                    case "harming":
                    case "strong_harming":
                        type = PotionType.INSTANT_DAMAGE;
                        break;
                    case "healing":
                    case "strong_healing":
                        type = PotionType.INSTANT_HEAL;
                        break;
                    case "invisibility":
                    case "long_invisibility":
                        type = PotionType.INVISIBILITY;
                        break;
                    case "leaping":
                    case "long_leaping":
                    case "strong_leaping":
                        type = PotionType.JUMP;
                        break;
                    case "luck":
                        type = PotionType.LUCK;
                        break;
                    case "night_vision":
                    case "long_night_vision":
                        type = PotionType.NIGHT_VISION;
                        break;
                    case "poison":
                    case "long_poison":
                    case "strong_poison":
                        type = PotionType.POISON;
                        break;
                    case "regeneration":
                    case "long_regeneration":
                    case "strong_regeneration":
                        type = PotionType.REGEN;
                        break;
                    case "slowness":
                    case "long_slowness":
                        type = PotionType.SLOWNESS;
                        break;
                    case "swiftness":
                    case "long_swiftness":
                    case "strong_swiftness":
                        type = PotionType.SPEED;
                        break;
                    case "strength":
                    case "long_strength":
                    case "strong_strength":
                        type = PotionType.STRENGTH;
                        break;
                    case "water_breathing":
                    case "long_water_breathing":
                        type = PotionType.WATER_BREATHING;
                        break;
                    case "water":
                        type = PotionType.WATER;
                        break;
                    case "weakness":
                    case "long_weakness":
                        type = PotionType.WEAKNESS;
                        break;
                    case "empty":
                        type = PotionType.EMPTY;
                        break;
                    case "mundane":
                        type = PotionType.MUNDANE;
                        break;
                    case "thick":
                        type = PotionType.THICK;
                        break;
                    case "awkward":
                        type = PotionType.AWKWARD;
                        break;
                    default:
                        return null;
                }

                return new Potions(type, strong, _long, item.getType().equals(Material.LINGERING_POTION), item.getType().equals(Material.SPLASH_POTION));
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
