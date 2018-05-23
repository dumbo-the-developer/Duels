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

package me.realized.duels.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import me.realized.duels.util.Loadable;
import org.bukkit.entity.Player;

public abstract class Cache<V> implements Loadable {

    private final Map<UUID, V> cache = new HashMap<>();

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        cache.clear();
    }

    abstract V create(final Player player);

    public Optional<V> get(final Player player) {
        return Optional.ofNullable(cache.get(player.getUniqueId()));
    }

    public V getSafely(final Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), result -> create(player));
    }

    public void put(final Player player) {
        cache.put(player.getUniqueId(), create(player));
    }

    public Optional<V> remove(final Player player) {
        return Optional.ofNullable(cache.remove(player.getUniqueId()));
    }
}
