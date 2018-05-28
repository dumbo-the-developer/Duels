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

package me.realized.duels.util.meta;

import java.util.Map;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public final class MetadataUtil {

    private MetadataUtil() {}

    public static Optional<Object> get(final Plugin plugin, final Player player, final String key) {
        final Optional<MetadataValue> cached = player.getMetadata(key).stream().filter(value -> value.getOwningPlugin().equals(plugin)).findFirst();
        return Optional.ofNullable(cached.isPresent() ? cached.get().value() : null);
    }

    public static void put(final Plugin plugin, final Player player, final String key, final Map<String, Object> data) {
        player.setMetadata(key, new MetadataValue() {
            @Override
            public Object value() {
                return data;
            }

            @Override
            public int asInt() { return 0; }

            @Override
            public float asFloat() { return 0; }

            @Override
            public double asDouble() { return 0; }

            @Override
            public long asLong() { return 0; }

            @Override
            public short asShort() { return 0; }

            @Override
            public byte asByte() { return 0; }

            @Override
            public boolean asBoolean() { return false; }

            @Override
            public String asString() { return null; }

            @Override
            public Plugin getOwningPlugin() { return plugin; }

            @Override
            public void invalidate() {}
        });
    }

    public static void remove(final Plugin plugin, final Player player, final String key) {
        player.removeMetadata(key, plugin);
    }

    public static Optional<Object> removeAndGet(final Plugin plugin, final Player player, final String key) {
        final Optional<Object> value = get(plugin, player, key);
        remove(plugin, player, key);
        return value;
    }
}
