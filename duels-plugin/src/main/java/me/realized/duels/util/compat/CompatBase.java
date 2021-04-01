package me.realized.duels.util.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.realized.duels.util.reflect.ReflectionUtil;
import org.bukkit.potion.PotionData;

class CompatBase {

    static final Method GET_HANDLE;
    static final Field PLAYER_CONNECTION;
    static final Method SEND_PACKET;

    static final Class<?> TITLE_PACKET;
    static final Class<?> CHAT_COMPONENT;

    static Class<?> TITLE_ACTIONS;

    static final Class<?> CHAT_SERIALIZER;

    static final Field CB_INVENTORY;
    static final Field CB_INVENTORY_TITLE;
    static final Method CHAT_SERIALIZER_A;

    static Method FROM_BUKKIT;

    static Method GET_PROFILE;
    static Field PROFILE;

    static {
        final Class<?> CB_PLAYER = ReflectionUtil.getCBClass("entity.CraftPlayer");
        GET_HANDLE = ReflectionUtil.getMethod(CB_PLAYER, "getHandle");

        final Class<?> NMS_PLAYER = ReflectionUtil.getNMSClass("EntityPlayer");
        PLAYER_CONNECTION = ReflectionUtil.getField(NMS_PLAYER, "playerConnection");
        SEND_PACKET = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("PlayerConnection"), "sendPacket", ReflectionUtil.getNMSClass("Packet"));

        TITLE_PACKET = ReflectionUtil.getNMSClass("PacketPlayOutTitle");
        CHAT_COMPONENT = ReflectionUtil.getNMSClass("IChatBaseComponent");
        TITLE_ACTIONS = ReflectionUtil.getNMSClass("PacketPlayOutTitle$EnumTitleAction", false);

        if (TITLE_ACTIONS == null) {
            TITLE_ACTIONS = ReflectionUtil.getNMSClass("EnumTitleAction");
        }

        CHAT_SERIALIZER = ReflectionUtil.getNMSClass("ChatComponentText");

        CB_INVENTORY = ReflectionUtil.getDeclaredField(ReflectionUtil.getCBClass("inventory.CraftInventory"), "inventory");
        CB_INVENTORY_TITLE = ReflectionUtil.getDeclaredField(ReflectionUtil.getCBClass("inventory.CraftInventoryCustom$MinecraftInventory"), "title");
        CHAT_SERIALIZER_A =
            !CompatUtil.isPre1_13() ? ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("IChatBaseComponent$ChatSerializer"), "a", String.class) : null;

        final Class<?> POTION_UTIL = ReflectionUtil.getCBClass("potion.CraftPotionUtil", false);

        if (POTION_UTIL != null) {
            FROM_BUKKIT = ReflectionUtil.getMethod(POTION_UTIL, "fromBukkit", PotionData.class);
        }

        GET_PROFILE = ReflectionUtil.getMethod(NMS_PLAYER, "getProfile");
        PROFILE = ReflectionUtil.getDeclaredField(ReflectionUtil.getCBClass("inventory.CraftMetaSkull"), "profile");
    }
}
