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

package me.realized.duels.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Loadable;
import org.bukkit.entity.Player;

public class PlayerInfoManager implements Loadable {

    private final Map<UUID, PlayerInfo> cache = new HashMap<>();

    public PlayerInfoManager(final DuelsPlugin plugin) {}

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {}

    public PlayerInfo get(final Player player) {
        return cache.get(player.getUniqueId());
    }

    public void put(final Player player, final PlayerInfo info) {
        cache.put(player.getUniqueId(), info);
    }

    public void remove(final Player player) {
        cache.remove(player.getUniqueId());
    }

    public PlayerInfo removeAndGet(final Player player) {
        final PlayerInfo info = get(player);
        remove(player);
        return info;
    }
}
