/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.realized.duels.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Log;
import me.realized.duels.util.Reloadable;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.config.AbstractConfiguration;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

public class Lang extends AbstractConfiguration<DuelsPlugin> implements Reloadable {

    private final Map<String, String> messages = new HashMap<>();

    public Lang(final DuelsPlugin plugin) {
        super(plugin, "lang");
    }

    @Override
    protected void loadValues(FileConfiguration configuration) throws IOException {
        // TODO: 26/04/2018 Temp disabled to allow /reload, enable later
//        if (configuration.getInt("config-version") < getLatestVersion()) {
//            configuration = convert(null);
//        }

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

                if (StringUtils.containsIgnoreCase(value, placeholder)) {
                    value = value.replaceAll("(?i)" + Pattern.quote(placeholder), entry.getValue());
                }
            }

            return value;
        });
    }

    @Override
    public void handleUnload() {
        messages.clear();
    }

    public void sendMessage(final CommandSender receiver, final String key, final Object... replacers) {
        final String message = getMessage(key, replacers);

        if (message == null) {
            return;
        }

        receiver.sendMessage(message);
    }

    public void sendMessage(final String key, final CommandSender... receivers) {
        final String message = getMessage(key);

        if (message == null) {
            return;
        }

        Arrays.stream(receivers).forEach(receiver -> receiver.sendMessage(message));
    }

    public String getMessage(final String key, final Object... replacers) {
        final String message = messages.get(key);

        if (message == null) {
            Log.error(this, "Failed to send message: provided key '" + key + "' has no assigned value");
            return null;
        }

        // Allows disabling any message by setting it to ''
        if (message.isEmpty()) {
            return null;
        }

        return StringUtil.color(replace(message, replacers));
    }

    private String replace(String message, final Object... replacers) {
        for (int i = 0; i < replacers.length; i += 2) {
            if (i + 1 >= replacers.length) {
                break;
            }

            message = message.replace("%" + replacers[i].toString() + "%", String.valueOf(replacers[i + 1]));
        }

        return message;
    }
}
