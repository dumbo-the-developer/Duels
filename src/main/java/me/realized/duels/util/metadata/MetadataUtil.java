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

package me.realized.duels.util.metadata;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public final class MetadataUtil {

    private MetadataUtil() {}

    public static Object get(final Plugin plugin, final Player player, final String key) {
        return player.getMetadata(key).stream().filter(value -> value.getOwningPlugin().equals(plugin)).findFirst().map(MetadataValue::value).orElse(null);
    }

    public static void put(final Plugin plugin, final Player player, final String key, final Object data) {
        player.setMetadata(key, new FixedMetadataValue(plugin, data));
    }

    public static void remove(final Plugin plugin, final Player player, final String key) {
        player.removeMetadata(key, plugin);
    }

    public static Object removeAndGet(final Plugin plugin, final Player player, final String key) {
        final Object value = get(plugin, player, key);
        remove(plugin, player, key);
        return value;
    }
}
