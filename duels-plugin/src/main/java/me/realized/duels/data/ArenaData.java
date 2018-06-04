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

package me.realized.duels.data;

import java.util.HashMap;
import java.util.Map;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import org.bukkit.Location;

public class ArenaData {

    private final String name;
    private final boolean disabled;
    private final Map<Integer, LocationData> positions = new HashMap<>();

    public ArenaData(final Arena arena) {
        this.name = arena.getName();
        this.disabled = arena.isDisabled();

        for (final Map.Entry<Integer, Location> entry : arena.getPositions().entrySet()) {
            positions.put(entry.getKey(), new LocationData(entry.getValue()));
        }
    }

    public Arena toArena(final DuelsPlugin plugin) {
        final Arena arena = new Arena(plugin, name);
        arena.setDisabled(disabled);

        for (final Map.Entry<Integer, LocationData> entry : positions.entrySet()) {
            arena.setPosition(entry.getKey(), entry.getValue().toLocation());
        }

        return arena;
    }
}
