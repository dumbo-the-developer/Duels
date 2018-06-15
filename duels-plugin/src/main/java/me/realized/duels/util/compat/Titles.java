package me.realized.duels.util.compat;

import java.lang.reflect.Constructor;
import me.realized.duels.util.StringUtil;
import org.bukkit.entity.Player;

public final class Titles extends CompatBase {

    private Titles() {}

    public static void send(final Player player, final String title, final String subtitle, final int fadeIn, final int stay, final int fadeOut) {
        if (CompatUtil.isPre1_8()) {
            return;
        }

        if (CompatUtil.isPre1_9()) {
            try {
                final Object connection = PLAYER_CONNECTION.get(GET_HANDLE.invoke(player));
                final Object[] actions = TITLE_ACTIONS.getEnumConstants();
                Constructor<?> constructor = TITLE_PACKET.getConstructor(TITLE_ACTIONS, CHAT_COMPONENT, int.class, int.class, int.class);
                SEND_PACKET.invoke(connection, constructor.newInstance(actions[2], null, fadeIn, stay, fadeOut));
                constructor = TITLE_PACKET.getConstructor(TITLE_ACTIONS, CHAT_COMPONENT);
                Object text = CHAT_SERIALIZER.getConstructor(String.class).newInstance(StringUtil.color(title));
                SEND_PACKET.invoke(connection, constructor.newInstance(actions[0], text));

                if (subtitle != null) {
                    constructor = TITLE_PACKET.getConstructor(TITLE_ACTIONS, CHAT_COMPONENT);
                    text = CHAT_SERIALIZER.getConstructor(String.class).newInstance(StringUtil.color(subtitle));
                    SEND_PACKET.invoke(connection, constructor.newInstance(actions[1], text));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            player.sendTitle(StringUtil.color(title), subtitle != null ? StringUtil.color(subtitle) : null, fadeIn, stay, fadeOut);
        }
    }
}
