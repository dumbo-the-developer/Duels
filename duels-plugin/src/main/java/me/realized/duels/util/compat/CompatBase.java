package me.realized.duels.util.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

class CompatBase {

    static final Method AS_NMS_COPY;
    static final Method AS_BUKKIT_COPY;

    static final Class<?> TAG_COMPOUND;
    static final Method GET_TAG;
    static final Method SET_TAG;
    static final Method GET;
    static final Method SET;
    static final Method ADD;
    static final Method SIZE;
    static final Method GET_LIST;
    static final Method GET_STRING;
    static final Method SET_STRING;
    static final Method GET_INT;
    static final Method SET_INT;
    static final Method GET_DOUBLE;
    static final Method SET_DOUBLE;
    static final Method SET_LONG;
    static final Method GET_COMPOUND;

    static final Method GET_HANDLE;
    static final Field PLAYER_CONNECTION;
    static final Method SEND_PACKET;
    static final Field COLLIDES_WITH_ENTITIES;

    static final Method GET_ONLINE_PLAYERS;

    static final Class<?> TITLE_PACKET;
    static final Class<?> CHAT_COMPONENT;
    static Class<?> TITLE_ACTIONS;
    static final Class<?> CHAT_SERIALIZER;

    static final Field CB_INVENTORY;
    static final Field CB_INVENTORY_TITLE;
    static final Method CHAT_SERIALIZER_A;

    static {
        final Class<?> CB_ITEMSTACK = ReflectionUtil.getCBClass("inventory.CraftItemStack");
        final Class<?> NMS_ITEMSTACK = ReflectionUtil.getNMSClass("ItemStack");
        AS_NMS_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asNMSCopy", ItemStack.class);
        AS_BUKKIT_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asBukkitCopy", NMS_ITEMSTACK);
        TAG_COMPOUND = ReflectionUtil.getNMSClass("NBTTagCompound");

        final Class<?> TAG_LIST = ReflectionUtil.getNMSClass("NBTTagList");
        final Class<?> TAG_BASE = ReflectionUtil.getNMSClass("NBTBase");
        GET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "getTag");
        SET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "setTag", TAG_COMPOUND);
        SET = ReflectionUtil.getMethod(TAG_COMPOUND, "set", String.class, TAG_BASE);
        GET_LIST = ReflectionUtil.getMethod(TAG_COMPOUND, "getList", String.class, int.class);
        GET = ReflectionUtil.getMethod(TAG_LIST, "get", int.class);
        ADD = ReflectionUtil.getMethod(TAG_LIST, "add", TAG_BASE);
        SIZE = ReflectionUtil.getMethod(TAG_LIST, "size");
        GET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "getString", String.class);
        SET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "setString", String.class, String.class);
        GET_INT = ReflectionUtil.getMethod(TAG_COMPOUND, "getInt", String.class);
        SET_INT = ReflectionUtil.getMethod(TAG_COMPOUND, "setInt", String.class, int.class);
        GET_DOUBLE = ReflectionUtil.getMethod(TAG_COMPOUND, "getDouble", String.class);
        SET_DOUBLE = ReflectionUtil.getMethod(TAG_COMPOUND, "setDouble", String.class, double.class);
        SET_LONG = ReflectionUtil.getMethod(TAG_COMPOUND, "setLong", String.class, long.class);
        GET_COMPOUND = ReflectionUtil.getMethod(TAG_COMPOUND, "getCompound", String.class);

        final Class<?> CB_PLAYER = ReflectionUtil.getCBClass("entity.CraftPlayer");
        GET_HANDLE = ReflectionUtil.getMethod(CB_PLAYER, "getHandle");

        final Class<?> NMS_PLAYER = ReflectionUtil.getNMSClass("EntityPlayer");
        PLAYER_CONNECTION = ReflectionUtil.getField(NMS_PLAYER, "playerConnection");
        SEND_PACKET = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("PlayerConnection"), "sendPacket", ReflectionUtil.getNMSClass("Packet"));

        COLLIDES_WITH_ENTITIES = !CompatUtil.hasSetCollidable() ? ReflectionUtil.getField(NMS_PLAYER, "collidesWithEntities") : null;

        GET_ONLINE_PLAYERS = ReflectionUtil.getMethod(Bukkit.class, "getOnlinePlayers");

        boolean pre1_8 = CompatUtil.isPre1_8();
        TITLE_PACKET = !pre1_8 ? ReflectionUtil.getNMSClass("PacketPlayOutTitle") : null;
        CHAT_COMPONENT = !pre1_8 ? ReflectionUtil.getNMSClass("IChatBaseComponent") : null;
        TITLE_ACTIONS = !pre1_8 ? ReflectionUtil.getNMSClass("PacketPlayOutTitle$EnumTitleAction", false) : null;

        if (!pre1_8 && TITLE_ACTIONS == null) {
            TITLE_ACTIONS = ReflectionUtil.getNMSClass("EnumTitleAction");
        }

        CHAT_SERIALIZER = !pre1_8 ? ReflectionUtil.getNMSClass("ChatComponentText") : null;

        CB_INVENTORY = ReflectionUtil.getDeclaredField(ReflectionUtil.getCBClass("inventory.CraftInventory"), "inventory");
        CB_INVENTORY_TITLE = ReflectionUtil.getDeclaredField(ReflectionUtil.getCBClass("inventory.CraftInventoryCustom$MinecraftInventory"), "title");
        CHAT_SERIALIZER_A =
            !CompatUtil.isPre1_13() ? ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("IChatBaseComponent$ChatSerializer"), "a", String.class) : null;
    }
}
