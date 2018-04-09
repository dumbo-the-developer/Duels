/*
 *
 *   This file is part of TokenManager, licensed under the MIT License.
 *
 *   Copyright (c) Realized
 *   Copyright (c) contributors
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 *
 */

package me.realized.duels.util.profile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ProfileUtil {

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");
    private static boolean USING_SPIGOT;

    static {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            USING_SPIGOT = true;
        } catch (ClassNotFoundException ignored) {}
    }

    private ProfileUtil() {}

    public static boolean isOnlineMode() {
        final boolean online;
        return !(online = Bukkit.getOnlineMode()) && USING_SPIGOT && Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")
            || online;
    }

    public static boolean isUUID(final String s) {
        return UUID_PATTERN.matcher(s).matches();
    }

    public static void getNames(final List<UUID> uuids, final Consumer<Map<UUID, String>> consumer) {
        NameFetcher.getNames(uuids, consumer);
    }

    public static void getUUID(final String name, final Consumer<String> consumer, final Consumer<String> errorHandler) {
        final Player player;

        if ((player = Bukkit.getPlayerExact(name)) != null) {
            consumer.accept(player.getUniqueId().toString());
            return;
        }

        try {
            consumer.accept(UUIDFetcher.getUUID(name));
        } catch (Exception ex) {
            errorHandler.accept(ex.getMessage());
        }
    }
}
