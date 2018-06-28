package me.realized.duels.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public final class TextBuilder {

    private final List<BaseComponent> list = new ArrayList<>();

    private TextBuilder(final String base) {
        if (base == null) {
            return;
        }

        list.addAll(Arrays.asList(TextComponent.fromLegacyText(base)));
    }

    public static TextBuilder of(final String base) {
        return new TextBuilder(base);
    }

    public TextBuilder add(final String text) {
        if (text == null) {
            return this;
        }

        list.addAll(Arrays.asList(TextComponent.fromLegacyText(text)));
        return this;
    }

    public TextBuilder add(final String text, final ClickEvent.Action action, final String value) {
        if (text == null || value == null) {
            return this;
        }

        Arrays.stream(TextComponent.fromLegacyText(text)).forEach(component -> {
            component.setClickEvent(new ClickEvent(action, value));
            list.add(component);
        });
        return this;
    }

    public TextBuilder add(final String text, final HoverEvent.Action action, final String value) {
        if (text == null || value == null) {
            return this;
        }

        Arrays.stream(TextComponent.fromLegacyText(text)).forEach(component -> {
            component.setHoverEvent(new HoverEvent(action, TextComponent.fromLegacyText(value)));
            list.add(component);
        });
        return this;
    }

    public TextBuilder setClickEvent(final ClickEvent.Action action, final String value) {
        if (value == null) {
            return this;
        }

        list.forEach(component -> component.setClickEvent(new ClickEvent(action, value)));
        return this;
    }

    public TextBuilder setHoverEvent(final HoverEvent.Action action, final String value) {
        if (value == null) {
            return this;
        }

        list.forEach(component -> component.setHoverEvent(new HoverEvent(action, TextComponent.fromLegacyText(value))));
        return this;
    }

    public void send(final Collection<Player> players) {
        final BaseComponent[] message = list.toArray(new BaseComponent[list.size()]);
        players.forEach(player -> {
            if (player.isOnline()) {
                player.spigot().sendMessage(message);
            }
        });
    }

    public void send(final Player... players) {
        send(Arrays.asList(players));
    }
}
