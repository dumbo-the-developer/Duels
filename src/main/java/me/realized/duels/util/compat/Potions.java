package me.realized.duels.util.compat;

import java.util.Collection;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

public class Potions extends CompatBase {

    @Getter
    private final PotionType type;
    @Getter
    private final boolean strong, extended, linger, splash;

    private Potions(final PotionType type, final boolean strong, final boolean extended, final boolean linger, final boolean splash) {
        this.type = type;
        this.strong = strong;
        this.extended = extended;
        this.linger = linger;
        this.splash = splash;
    }

    public Potions(PotionType type, Collection<String> args) {
        this(type, args.contains("strong"), args.contains("extended"), args.contains("linger"), args.contains("splash"));
    }

    public static Potions fromItemStack(final ItemStack item) {
        if (item == null || !item.getType().name().contains("POTION")) {
            return null;
        }

        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);
            final Object tag = GET_TAG.invoke(nmsItem);
            String potionType = (String) GET_STRING.invoke(tag, "Potion");
            potionType = potionType.replace("minecraft:", "");
            PotionType type;
            final boolean strong = potionType.contains("strong");
            final boolean _long = potionType.contains("long");

            switch (potionType) {
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

            return new Potions(type, strong, _long, item.getType() == Material.LINGERING_POTION, item.getType() == Material.SPLASH_POTION);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ItemStack toItemStack(final int amount) {
        final ItemStack item = new ItemStack(!splash ? (linger ? Material.LINGERING_POTION : Material.POTION) : Material.SPLASH_POTION, amount);

        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);
            Object tag = GET_TAG.invoke(nmsItem);

            if (tag == null) {
                tag = TAG_COMPOUND.newInstance();
            }

            String potionType;

            switch (type) {
                case UNCRAFTABLE:
                    potionType = "empty";
                    break;
                case WATER:
                    potionType = "water";
                    break;
                case MUNDANE:
                    potionType = "mundane";
                    break;
                case THICK:
                    potionType = "thick";
                    break;
                case AWKWARD:
                    potionType = "awkward";
                    break;
                case NIGHT_VISION:
                    if (extended) {
                        potionType = "long_night_vision";
                    } else {
                        potionType = "night_vision";
                    }
                    break;
                case INVISIBILITY:
                    if (extended) {
                        potionType = "long_invisibility";
                    } else {
                        potionType = "invisibility";
                    }
                    break;
                case JUMP:
                    if (extended) {
                        potionType = "long_leaping";
                    } else if (strong) {
                        potionType = "strong_leaping";
                    } else {
                        potionType = "leaping";
                    }
                    break;
                case FIRE_RESISTANCE:
                    if (extended) {
                        potionType = "long_fire_resistance";
                    } else {
                        potionType = "fire_resistance";
                    }
                    break;
                case SPEED:
                    if (extended) {
                        potionType = "long_swiftness";
                    } else if (strong) {
                        potionType = "strong_swiftness";
                    } else {
                        potionType = "swiftness";
                    }
                    break;
                case SLOWNESS:
                    if (extended) {
                        potionType = "long_slowness";
                    } else {
                        potionType = "slowness";
                    }
                    break;
                case WATER_BREATHING:
                    if (extended) {
                        potionType = "long_water_breathing";
                    } else {
                        potionType = "water_breathing";
                    }
                    break;
                case INSTANT_HEAL:
                    if (strong) {
                        potionType = "strong_healing";
                    } else {
                        potionType = "healing";
                    }
                    break;
                case INSTANT_DAMAGE:
                    if (strong) {
                        potionType = "strong_harming";
                    } else {
                        potionType = "harming";
                    }
                    break;
                case POISON:
                    if (extended) {
                        potionType = "long_poison";
                    } else if (strong) {
                        potionType = "strong_poison";
                    } else {
                        potionType = "poison";
                    }
                    break;
                case REGEN:
                    if (extended) {
                        potionType = "long_regeneration";
                    } else if (strong) {
                        potionType = "strong_regeneration";
                    } else {
                        potionType = "regeneration";
                    }
                    break;
                case STRENGTH:
                    if (extended) {
                        potionType = "long_strength";
                    } else if (strong) {
                        potionType = "strong_strength";
                    } else {
                        potionType = "strength";
                    }
                    break;
                case WEAKNESS:
                    if (extended) {
                        potionType = "long_weakness";
                    } else {
                        potionType = "weakness";
                    }
                    break;
                case LUCK:
                    potionType = "luck";
                    break;
                default:
                    return null;
            }

            SET_STRING.invoke(tag, "Potion", "minecraft:" + potionType);
            SET_TAG.invoke(nmsItem, tag);
            return (ItemStack) AS_BUKKIT_COPY.invoke(null, nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
