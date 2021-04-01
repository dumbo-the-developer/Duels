package me.realized.duels.util.compat.nbt;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import me.realized.duels.util.compat.CompatUtil;
import me.realized.duels.util.reflect.ReflectionUtil;
import org.bukkit.inventory.ItemStack;

public final class NBT {

    private static boolean ADD_INSERT;

    private static final Method AS_NMS_COPY;
    private static final Method AS_BUKKIT_COPY;

    private static final Method PARSE;

    private static final Class<?> TAG_BASE;
    private static final Class<?> TAG_COMPOUND;
    private static final Class<?> TAG_STRING;

    private static final Method NEW_STRING;
    private static final Method GET_TAG;
    private static final Method SET_TAG;
    private static final Method SET;
    private static final Method ADD;
    private static final Method GET_LIST;
    private static final Method SET_STRING;
    private static final Method SET_INT;
    private static final Method SET_DOUBLE;
    private static final Method SET_BOOLEAN;
    private static final Method SET_SHORT;
    private static final Method SET_BYTE;
    private static final Method SET_LONG;
    private static final Method GET_COMPOUND;
    private static final Method REMOVE;
    private static final Method HAS_KEY;

    private static final Method GAME_PROFILE_SERIALIZE;

    static {
        final Class<?> CB_ITEMSTACK = ReflectionUtil.getCBClass("inventory.CraftItemStack");
        final Class<?> NMS_ITEMSTACK = ReflectionUtil.getNMSClass("ItemStack");
        AS_NMS_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asNMSCopy", ItemStack.class);
        AS_BUKKIT_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asBukkitCopy", NMS_ITEMSTACK);

        final Class<?> MOJANGSON_PARSER = ReflectionUtil.getNMSClass("MojangsonParser");
        PARSE = ReflectionUtil.getMethod(MOJANGSON_PARSER, "parse", String.class);

        TAG_COMPOUND = ReflectionUtil.getNMSClass("NBTTagCompound");
        TAG_BASE = ReflectionUtil.getNMSClass("NBTBase");
        TAG_STRING = ReflectionUtil.getNMSClass("NBTTagString");

        final Class<?> TAG_LIST = ReflectionUtil.getNMSClass("NBTTagList");

        NEW_STRING = ReflectionUtil.getMethodUnsafe(TAG_STRING, "a", String.class);
        GET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "getTag");
        SET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "setTag", TAG_COMPOUND);
        SET = ReflectionUtil.getMethod(TAG_COMPOUND, "set", String.class, TAG_BASE);
        GET_LIST = ReflectionUtil.getMethod(TAG_COMPOUND, "getList", String.class, int.class);
        ADD_INSERT = ReflectionUtil.getMethodUnsafe(TAG_LIST, "add", TAG_BASE) == null;
        ADD = ADD_INSERT ? ReflectionUtil.getMethod(TAG_LIST, "add", Integer.TYPE, TAG_BASE) : ReflectionUtil.getMethod(TAG_LIST, "add", TAG_BASE);
        SET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "setString", String.class, String.class);
        SET_INT = ReflectionUtil.getMethod(TAG_COMPOUND, "setInt", String.class, int.class);
        SET_DOUBLE = ReflectionUtil.getMethod(TAG_COMPOUND, "setDouble", String.class, double.class);
        SET_BOOLEAN = ReflectionUtil.getMethod(TAG_COMPOUND, "setBoolean", String.class, boolean.class);
        SET_SHORT = ReflectionUtil.getMethod(TAG_COMPOUND, "setShort", String.class, short.class);
        SET_BYTE = ReflectionUtil.getMethod(TAG_COMPOUND, "setByte", String.class, byte.class);
        SET_LONG = ReflectionUtil.getMethod(TAG_COMPOUND, "setLong", String.class, long.class);
        GET_COMPOUND = ReflectionUtil.getMethod(TAG_COMPOUND, "getCompound", String.class);
        REMOVE = ReflectionUtil.getMethod(TAG_COMPOUND, "remove", String.class);
        HAS_KEY = ReflectionUtil.getMethod(TAG_COMPOUND, "hasKey", String.class);

        GAME_PROFILE_SERIALIZE = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("GameProfileSerializer"), "serialize", TAG_COMPOUND, GameProfile.class);
    }

    public static Object getProfileTag(final String username) {
        try {
            final Object tag = newTag();
            final GameProfile profile = new GameProfile(null, username);
            GAME_PROFILE_SERIALIZE.invoke(null, tag, profile);
            return tag;
        } catch (InvocationTargetException | IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Object newString(final String value) {
        try {
            if (CompatUtil.isPre1_15()) {
                return TAG_STRING.getConstructor(String.class).newInstance(value);
            }

            return NEW_STRING.invoke(null, value);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Object newTag() {
        try {
            return TAG_COMPOUND.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Object getOrCreateTag(final Object tag, final String key) {
        try {
            return GET_COMPOUND.invoke(tag, key);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void set(final Object tag, final String key, final Object value) {
        try {
            SET.invoke(tag, key, value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static void setString(final Object tag, final String key, final String value) {
        try {
            SET_STRING.invoke(tag, key, value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static void setInt(final Object tag, final String key, final int value) {
        try {
            SET_INT.invoke(tag, key, value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static void setDouble(final Object tag, final String key, final double value) {
        try {
            SET_DOUBLE.invoke(tag, key, value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static void setByte(final Object tag, final String key, final byte value) {
        try {
            SET_BYTE.invoke(tag, key, value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static void setShort(final Object tag, final String key, final short value) {
        try {
            SET_SHORT.invoke(tag, key, value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static void setLong(final Object tag, final String key, final long value) {
        try {
            SET_LONG.invoke(tag, key, value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static void setBoolean(final Object tag, final String key, final boolean value) {
        try {
            SET_BOOLEAN.invoke(tag, key, value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static Object getOrCreateList(final Object tag, final String key, final int type) {
        try {
            return GET_LIST.invoke(tag, key, type);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void add(final Object list, final Object value) {
        try {
            if (ADD_INSERT) {
                ADD.invoke(list, 0, value);
            } else {
                ADD.invoke(list, value);
            }
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static String toString(final Object tag) {
        if (!TAG_BASE.isInstance(tag)) {
            return "";
        }

        return tag.toString();
    }

    public static Object getTag(final ItemStack item) {
        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);
            return GET_TAG.invoke(nmsItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return item;
        }
    }

    public static ItemStack parseAndSetTag(final ItemStack item, final String nbt) {
        try {
            final Object base = PARSE.invoke(null, nbt);
            final Object tag = TAG_COMPOUND.cast(base);
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);
            SET_TAG.invoke(nmsItem, tag);
            return (ItemStack) AS_BUKKIT_COPY.invoke(null, nmsItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return item;
        }
    }

    public static ItemStack setItemString(final ItemStack item, final String key, final Object value) {
        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);
            Object tag = GET_TAG.invoke(nmsItem);

            if (tag == null) {
                tag = TAG_COMPOUND.newInstance();
            }

            SET_STRING.invoke(tag, key, value.toString());
            SET_TAG.invoke(nmsItem, tag);
            return (ItemStack) AS_BUKKIT_COPY.invoke(null, nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
            return item;
        }
    }

    public static ItemStack removeItemTag(final ItemStack item, final String key) {
        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);
            Object tag = GET_TAG.invoke(nmsItem);

            if (tag == null) {
                return item;
            }

            REMOVE.invoke(tag, key);
            SET_TAG.invoke(nmsItem, tag);
            return (ItemStack) AS_BUKKIT_COPY.invoke(null, nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
            return item;
        }
    }

    public static boolean hasItemKey(final ItemStack item, final String key) {
        try {
            final Object nmsItem = AS_NMS_COPY.invoke(null, item);

            if (nmsItem == null) {
                return false;
            }

            final Object tag = GET_TAG.invoke(nmsItem);
            return tag != null && (boolean) HAS_KEY.invoke(tag, key);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private NBT() {}
}
