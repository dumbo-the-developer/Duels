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

package me.realized._duels.dueling;

import java.util.UUID;
import me.realized._duels.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class Spectator {

    private final UUID owner;
    private final String spectating;
    private final Location base;
    private final Arena target;

    public Spectator(Player base, String spectating, Arena target) {
        this.owner = base.getUniqueId();
        this.spectating = spectating;
        this.base = base.getLocation().clone();
        this.target = target;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getSpectatingName() {
        return spectating;
    }

    public Location getBase() {
        return base;
    }

    public Arena getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Spectator spectator = (Spectator) o;

        return owner != null ? owner.equals(spectator.owner) : spectator.owner == null;
    }

    @Override
    public int hashCode() {
        return owner != null ? owner.hashCode() : 0;
    }
}
