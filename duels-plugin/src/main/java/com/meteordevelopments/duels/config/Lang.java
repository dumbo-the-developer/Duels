package com.meteordevelopments.duels.config;

import com.google.common.collect.Sets;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.Reloadable;
import com.meteordevelopments.duels.util.StringUtil;
import com.meteordevelopments.duels.util.config.AbstractConfiguration;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Pattern;

public class Lang extends AbstractConfiguration<DuelsPlugin> implements Reloadable {

    private final Config config;
    private final Map<String, String> messages = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public Lang(final DuelsPlugin plugin) {
        super(plugin, "lang");
        this.config = plugin.getConfiguration();
    }

    @Override
    protected void loadValues(FileConfiguration configuration) throws Exception {
        if (configuration.getInt("config-version", 0) < getLatestVersion()) {
            configuration = convert(null);
        }

        final Map<String, String> strings = new HashMap<>();

        for (String key : configuration.getKeys(true)) {
            if (key.equals("config-version")) {
                continue;
            }

            // Fixes a weird occurrence with FileConfiguration#getKeys that an extra separator char is prepended when called after FileConfiguration#set
            if (key.startsWith(".")) {
                key = key.substring(1);
            }

            final Object value = configuration.get(key);

            if (value == null || value instanceof MemorySection) {
                continue;
            }

            final String message = value instanceof List ? StringUtil.fromList((List<?>) value) : value.toString();

            if (key.startsWith("STRINGS")) {
                final String[] args = key.split(Pattern.quote("."));
                strings.put(args[args.length - 1], message);
            } else {
                messages.put(key, message);
            }
        }

        messages.replaceAll((key, value) -> {
            for (final Map.Entry<String, String> entry : strings.entrySet()) {
                final String placeholder = "{" + entry.getKey() + "}";

                if (StringUtil.containsIgnoreCase(value, placeholder)) {
                    value = value.replaceAll("(?i)" + Pattern.quote(placeholder), entry.getValue());
                }
            }

            return value;
        });
    }

    @Override
    protected Set<String> transferredSections() {
        return Sets.newHashSet("STRINGS");
    }

    @Override
    public void handleUnload() {
        messages.clear();
    }

    private String getRawMessage(final String key) {
        final String message = messages.get(key);

        if (message == null) {
            Log.error(this, "Failed to load message: provided key '" + key + "' has no assigned value");
            return null;
        }

        // Allow disabling any message by setting it to ''
        return !message.isEmpty() ? message : null;
    }

    public String getMessage(final String key) {
        final String message = getRawMessage(key);
        return message != null ? StringUtil.color(message) : null;
    }

    private String replace(String message, Object... replacers) {
        // If given an array of replacers as a single parameter, expand the array.
        if (replacers.length == 1 && replacers[0] instanceof Object[]) {
            replacers = (Object[]) replacers[0];
        }

        for (int i = 0; i < replacers.length; i += 2) {
            if (i + 1 >= replacers.length) {
                break;
            }

            message = message.replace("%" + replacers[i].toString() + "%", String.valueOf(replacers[i + 1]));
        }

        return message;
    }

    public String getMessage(final String key, final Object... replacers) {
        final String message = getMessage(key);
        return message != null ? replace(message, replacers) : null;
    }

    public void sendMessage(final CommandSender receiver, final String key, final Object... replacers) {
        final String message = getRawMessage(key);

        if (message == null) {
            return;
        }

        String replacedMessage = replace(message, replacers);

        if (receiver instanceof Player player) {
            if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                replacedMessage = PlaceholderAPI.setPlaceholders(player, replacedMessage);
            }
            replacedMessage = StringUtil.color(replacedMessage);
            config.playSound(player, replacedMessage);
            if (config.isUseMinimessage()) {
                Component parsed = miniMessage.deserialize(replacedMessage);
                player.sendMessage(parsed);
            }else{
                player.sendMessage(replacedMessage);
            }

        } else {
            // Console or other senders — no PlaceholderAPI parsing
            replacedMessage = StringUtil.color(replacedMessage);
            if (config.isUseMinimessage()) {
                Component parsed = miniMessage.deserialize(replacedMessage);
                receiver.sendMessage(parsed);
            }else {
                receiver.sendMessage(replacedMessage);
            }
        }
    }

    public void sendMessage(final Collection<Player> players, final String key, final Object... replacers) {
        players.forEach(player -> sendMessage(player, key, replacers));
    }

    /**
     * Converts a MiniMessage string to legacy format for GUI items and signs.
     * If MiniMessage is disabled, returns the original string with legacy color codes.
     */
    public String toLegacyString(final String message) {
        if (message == null) {
            return null;
        }

        if (config.isUseMinimessage()) {
            try {
                Component component = miniMessage.deserialize(message);
                return LegacyComponentSerializer.legacySection().serialize(component);
            } catch (Exception e) {
                // Fallback to legacy formatting if MiniMessage fails
                return StringUtil.color(message);
            }
        } else {
            return StringUtil.color(message);
        }
    }

    /**
     * Converts a MiniMessage string to legacy format for GUI items and signs with replacements.
     */
    public String toLegacyString(final String key, final Object... replacers) {
        final String message = getRawMessage(key);
        if (message == null) {
            return null;
        }

        String replacedMessage = replace(message, replacers);
        return toLegacyString(replacedMessage);
    }

}
