package me.realized.duels.data;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import me.realized.duels.util.EnumUtil;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.CompatUtil;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.compat.Potions;
import me.realized.duels.util.compat.nbt.NBT;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class ItemData {

    public static transient final ItemDataDeserializer DESERIALIZER = new ItemDataDeserializer();
    public static transient final String DUELS_ITEM_IDENTIFIER = "DuelsKitContent";

    public static ItemData fromItemStack(final ItemStack item) {
        return new ItemData(item);
    }

    private String material;
    private int amount;
    private short data;

    private String nbt;

    // For Gson deserializer
    private ItemData() {}

    private ItemData(ItemStack item) {
        this.material = item.getType().name();
        this.amount = item.getAmount() == 0 ? 1 : item.getAmount();
        this.data = Items.getDurability(item);

        item = NBT.removeItemTag(item, DUELS_ITEM_IDENTIFIER);
        final Object tag = NBT.getTag(item);

        if (tag != null) {
            this.nbt = NBT.toString(tag);
        }
    }

    public ItemStack toItemStack() {
        final Material type = Material.getMaterial(material);

        if (type == null) {
            return null;
        }

        ItemStack item = new ItemStack(type, amount);
        Items.setDurability(item, data);

        if (nbt != null) {
            item = NBT.parseAndSetTag(item, nbt);
        }

        return NBT.setItemString(item, DUELS_ITEM_IDENTIFIER, true);
    }

    @Override
    public String toString() {
        return "{\"material\":" + material + ", \"amount\":" + amount + ", \"data\":" + data + ", \"nbt\":" + nbt + "}";
    }

    public static class ItemDataDeserializer implements JsonDeserializer<ItemData> {

        private static final Set<String> OLD_KEYS = Sets.newHashSet(
            "itemData", "attributeModifiers", "enchantments",
            "displayName", "lore", "flags", "owner", "author", "title",
            "contents", "effects", "color", "unbreakable"
        );

        @Override
        public ItemData deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context) throws JsonParseException {
            if (!element.isJsonObject()) {
                throw new JsonParseException("not a JsonObject");
            }

            final JsonObject object = element.getAsJsonObject();

            if (!object.has("material")) {
                throw new JsonParseException("material undefined");
            }

            final String material = object.get("material").getAsString();
            final ItemData data = new ItemData();
            data.material = material;
            data.amount = object.has("amount") ? object.get("amount").getAsInt() : 1;
            data.data = object.has("data") ? object.get("data").getAsShort() : 0;

            if (object.has("nbt")) {
                data.nbt = object.get("nbt").getAsString();
            } else if (OLD_KEYS.stream().anyMatch(object::has)) {
                // BACKWARD COMPATIBILITY SUPPORT:
                // If no nbt field is available, the given JsonObject may be an older ItemData without nbt support.
                // The old fields are converted into an nbt tag and then discarded.
                final Object tag = NBT.newTag();

                if (object.has("displayName")) {
                    final String displayName = StringUtil.color(object.get("displayName").getAsString());
                    final Object displayTag = NBT.getOrCreateTag(tag, "display");
                    NBT.setString(displayTag, "Name", displayName);
                    NBT.set(tag, "display", displayTag);
                }

                if (object.has("lore")) {
                    final Object displayTag = NBT.getOrCreateTag(tag, "display");
                    final Object loreList = NBT.getOrCreateList(displayTag, "Lore", 8);
                    object.getAsJsonArray("lore").forEach(jsonElement -> NBT.add(loreList, NBT.newString(StringUtil.color(jsonElement.getAsString()))));
                    NBT.set(displayTag, "Lore", loreList);
                    NBT.set(tag, "display", displayTag);
                }

                if (object.has("enchantments")) {
                    final boolean hasId = CompatUtil.hasEnchantmentId();
                    final String fieldName = hasId ? "ench" : "Enchantments";
                    final Object enchantsList = NBT.getOrCreateList(tag, fieldName, 10);
                    final JsonObject enchantments = object.getAsJsonObject("enchantments");
                    enchantments.entrySet().forEach(entry -> {
                        final Object enchantTag = NBT.newTag();
                        final Enchantment enchantment = Enchantment.getByName(entry.getKey());

                        if (enchantment == null) {
                            return;
                        }

                        if (hasId) {
                            NBT.setShort(enchantTag, "id", (short) CompatUtil.getEnchantmentId(Enchantment.getByName(entry.getKey())));
                        } else {
                            NBT.setString(enchantTag, "id", enchantment.getKey().toString());
                        }

                        NBT.setShort(enchantTag, "lvl", entry.getValue().getAsShort());
                        NBT.add(enchantsList, enchantTag);
                    });

                    NBT.set(tag, fieldName, enchantsList);
                }

                if (object.has("unbreakable") && object.get("unbreakable").getAsBoolean()) {
                    NBT.setBoolean(tag, "Unbreakable", true);
                }

                if (object.has("author")) {
                    NBT.setString(tag, "author", object.get("author").getAsString());
                }

                if (object.has("title")) {
                    NBT.setString(tag, "title", object.get("title").getAsString());
                }

                if (object.has("contents")) {
                    final Object pagesList = NBT.getOrCreateList(tag, "pages", 8);
                    object.getAsJsonArray("contents").forEach(jsonElement -> {
                        final String page = "{\"text\": \"" + StringUtil.color(jsonElement.getAsString()) + "\"}";
                        NBT.add(pagesList, NBT.newString(page));
                    });
                    NBT.set(tag, "pages", pagesList);
                }

                if (object.has("color")) {
                    final Object displayTag = NBT.getOrCreateTag(tag, "display");
                    final Color color = DyeColor.valueOf(object.get("color").getAsString()).getColor();
                    NBT.setInt(displayTag, "color", color.asRGB());
                    NBT.set(tag, "display", displayTag);
                }

                if (object.has("attributeModifiers")) {
                    final Object attributesList = NBT.getOrCreateList(tag, "AttributeModifiers", 10);
                    object.getAsJsonArray("attributeModifiers").forEach(jsonElement -> {
                        final JsonObject attributeObject = jsonElement.getAsJsonObject();
                        final Object attributeTag = NBT.newTag();
                        NBT.setString(attributeTag, "Name", attributeObject.get("name").getAsString());
                        NBT.setString(attributeTag, "AttributeName", attributeObject.get("attrName").getAsString());
                        NBT.setInt(attributeTag, "Operation", attributeObject.get("operation").getAsInt());
                        NBT.setDouble(attributeTag, "Amount", attributeObject.get("amount").getAsDouble());

                        final UUID random = UUID.randomUUID();
                        NBT.setLong(attributeTag, "UUIDMost", random.getMostSignificantBits());
                        NBT.setLong(attributeTag, "UUIDLeast", random.getLeastSignificantBits());

                        if (attributeObject.has("slot")) {
                            NBT.setString(attributeTag, "Slot", attributeObject.get("slot").getAsString());
                        }

                        NBT.add(attributesList, attributeTag);
                    });
                    NBT.set(tag, "AttributeModifiers", attributesList);
                }

                if (CompatUtil.hasItemFlag() && object.has("flags")) {
                    int hideFlag = 0;

                    for (final JsonElement jsonElement : object.getAsJsonArray("flags")) {
                        ItemFlag flag = EnumUtil.getByName(jsonElement.getAsString(), ItemFlag.class);

                        if (flag == null) {
                            continue;
                        }

                        hideFlag |= (byte) (1 << flag.ordinal());
                    }

                    NBT.setInt(tag, "HideFlags", hideFlag);
                }

                if (object.has("owner")) {
                    NBT.set(tag, "SkullOwner", NBT.getProfileTag(object.get("owner").getAsString()));
                }

                if (object.has("effects")) {
                    final Object effectsList = NBT.getOrCreateList(tag, "CustomPotionEffects", 10);
                    final JsonObject enchantments = object.getAsJsonObject("effects");
                    enchantments.entrySet().forEach(entry -> {
                        final Object effectTag = NBT.newTag();
                        final PotionEffectType effectType = PotionEffectType.getByName(entry.getKey());

                        if (effectType == null) {
                            return;
                        }

                        final String[] effectData = entry.getValue().getAsString().split("-");
                        NBT.setByte(effectTag, "Id", (byte) effectType.getId());
                        NBT.setByte(effectTag, "Amplifier", (byte) Integer.parseInt(effectData[1]));
                        NBT.setInt(effectTag, "Duration", Integer.parseInt(effectData[0]));
                        NBT.setBoolean(effectTag, "Ambient", true);
                        NBT.setBoolean(effectTag, "ShowParticles", true);
                        NBT.add(effectsList, effectTag);
                    });

                    NBT.set(tag, "CustomPotionEffects", effectsList);
                }

                if (!CompatUtil.isPre1_9() && object.has("itemData")) {
                    final List<String> args = Arrays.asList(object.get("itemData").getAsString().split("-"));

                    if (material.contains("POTION") || material.equals("TIPPED_ARROW")) {
                        final PotionType potionType = EnumUtil.getByName(args.get(0), PotionType.class);

                        if (potionType != null) {
                            final PotionData potionData = new PotionData(potionType, args.contains("extended"), args.contains("strong") || args.contains("upgraded"));
                            NBT.setString(tag, "Potion", Potions.fromBukkit(potionData));
                        }
                    } else if (CompatUtil.isPre1_13() && material.equals("MONSTER_EGG")) {
                        final EntityType entityType = EnumUtil.getByName(args.get(0), EntityType.class);

                        if (entityType != null) {
                            final Object idTag = NBT.newTag();
                            NBT.setString(idTag, "id", entityType.getName());
                            NBT.set(tag, "EntityTag", idTag);
                        }
                    }
                }

                data.nbt = NBT.toString(tag);
                System.out.println(data.nbt);
            }

            return data;
        }
    }
}
