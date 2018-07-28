package me.realized.duels.util.compat;

import java.util.Collection;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Potions extends CompatBase {

    public enum PotionType {

        FIRE_RESISTANCE, INSTANT_DAMAGE, INSTANT_HEAL,
        INVISIBILITY, JUMP, LUCK,
        NIGHT_VISION, POISON, REGEN,
        SLOWNESS, SPEED, STRENGTH,
        WATER, WATER_BREATHING, WEAKNESS,
        EMPTY, UNCRAFTABLE, MUNDANE, THICK, AWKWARD,
        TURTLE_MASTER, SLOW_FALLING
    }

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
                case "empty":
                    type = PotionType.EMPTY;
                    break;
                case "turtle_master":
                case "long_turtle_master":
                case "strong_turtle_master":
                    type = PotionType.TURTLE_MASTER;
                    break;
                case "slow_falling":
                case "long_slow_falling":
                    type = PotionType.SLOW_FALLING;
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
                case EMPTY:
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
                    potionType = extended ? "long_night_vision" : "night_vision";
                    break;
                case INVISIBILITY:
                    potionType = extended ? "long_invisibility" : "invisibility";
                    break;
                case JUMP:
                    potionType = extended ? "long_leaping" : (strong ? "strong_leaping" : "leaping");
                    break;
                case FIRE_RESISTANCE:
                    potionType = extended ? "long_fire_resistance" : "fire_resistance";
                    break;
                case SPEED:
                    potionType = extended ? "long_swiftness" : (strong ? "strong_swiftness" : "swiftness");
                    break;
                case SLOWNESS:
                    potionType = extended ? "long_slowness" : "slowness";
                    break;
                case WATER_BREATHING:
                    potionType = extended ? "long_water_breathing" : "water_breathing";
                    break;
                case INSTANT_HEAL:
                    potionType = strong ? "strong_healing" : "healing";
                    break;
                case INSTANT_DAMAGE:
                    potionType = strong ? "strong_harming" : "harming";
                    break;
                case POISON:
                    potionType = extended ? "long_poison" : (strong ? "strong_poison" : "poison");
                    break;
                case REGEN:
                    potionType = extended ? "long_regeneration" : (strong ? "strong_regeneration" : "regeneration");
                    break;
                case STRENGTH:
                    potionType = extended ? "long_strength" : (strong ? "strong_strength" : "strength");
                    break;
                case WEAKNESS:
                    potionType = extended ? "long_weakness" : "weakness";
                    break;
                case LUCK:
                    potionType = "luck";
                    break;
                case TURTLE_MASTER:
                    potionType = extended ? "long_turtle_master" : (strong ? "strong_turtle_master" : "turtle_master");
                    break;
                case SLOW_FALLING:
                    potionType = extended ? "long_slow_falling" : "slow_falling";
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
