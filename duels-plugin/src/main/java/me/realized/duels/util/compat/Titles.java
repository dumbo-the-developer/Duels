package me.realized.duels.util.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.reflect.ReflectionUtil;
import org.bukkit.entity.Player;


public final class Titles {

    private static Method GET_HANDLE;
    private static Field PLAYER_CONNECTION;
    private static Method SEND_PACKET;
    private static Class<?> TITLE_ACTIONS;
    private static Constructor<?> TITLE_PACKET_FULL_CONSTRUCTOR;
    private static Constructor<?> TITLE_PACKET_CONSTRUCTOR;
    private static Class<?> CHAT_SERIALIZER;

    static {
        // Use Player#sendTitle for 1.11+
        if (!CompatUtil.hasSendTitle()) {
            final Class<?> CB_PLAYER = ReflectionUtil.getCBClass("entity.CraftPlayer");
            GET_HANDLE = ReflectionUtil.getMethod(CB_PLAYER, "getHandle");

            final Class<?> NMS_PLAYER = ReflectionUtil.getNMSClass("EntityPlayer");
            PLAYER_CONNECTION = ReflectionUtil.getField(NMS_PLAYER, "playerConnection");

            final Class<?> NMS_PLAYER_CONNECTION = ReflectionUtil.getNMSClass("PlayerConnection");
            final Class<?> NMS_PACKET = ReflectionUtil.getNMSClass("Packet");
            SEND_PACKET = ReflectionUtil.getMethod(NMS_PLAYER_CONNECTION, "sendPacket", NMS_PACKET);

            final Class<?> TITLE_PACKET = ReflectionUtil.getNMSClass("PacketPlayOutTitle");
            final Class<?> CHAT_COMPONENT = ReflectionUtil.getNMSClass("IChatBaseComponent");
            TITLE_ACTIONS = ReflectionUtil.getNMSClass("PacketPlayOutTitle$EnumTitleAction");

            // For v1_8_R1, EnumTitleAction is not an inner class of PacketPlayOutTitle
            if (TITLE_ACTIONS == null) {
                TITLE_ACTIONS = ReflectionUtil.getNMSClass("EnumTitleAction", false);
            }

            TITLE_PACKET_FULL_CONSTRUCTOR = ReflectionUtil.getConstructor(TITLE_PACKET, TITLE_ACTIONS, CHAT_COMPONENT, int.class, int.class, int.class);
            TITLE_PACKET_CONSTRUCTOR = ReflectionUtil.getConstructor(TITLE_PACKET, TITLE_ACTIONS, CHAT_COMPONENT);
            CHAT_SERIALIZER = ReflectionUtil.getNMSClass("ChatComponentText");
        }
    }

    private Titles() {}

    public static void send(final Player player, final String title, final String subtitle, final int fadeIn, final int stay, final int fadeOut) {
        if (CompatUtil.hasSendTitle()) {
            player.sendTitle(StringUtil.color(title), subtitle != null ? StringUtil.color(subtitle) : null, fadeIn, stay, fadeOut);
        } else {
            try {
                final Object connection = PLAYER_CONNECTION.get(GET_HANDLE.invoke(player));
                final Object[] actions = TITLE_ACTIONS.getEnumConstants();
                SEND_PACKET.invoke(connection, TITLE_PACKET_FULL_CONSTRUCTOR.newInstance(actions[2], null, fadeIn, stay, fadeOut));
                Object text = CHAT_SERIALIZER.getConstructor(String.class).newInstance(StringUtil.color(title));
                SEND_PACKET.invoke(connection, TITLE_PACKET_CONSTRUCTOR.newInstance(actions[0], text));

                if (subtitle != null) {
                    text = CHAT_SERIALIZER.getConstructor(String.class).newInstance(StringUtil.color(subtitle));
                    SEND_PACKET.invoke(connection, TITLE_PACKET_CONSTRUCTOR.newInstance(actions[1], text));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
