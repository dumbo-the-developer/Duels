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

package me.realized.duels.util.compat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Players extends CompatBase {

    private Players() {}

    /**
     * Handles {@link Bukkit#getOnlinePlayers()} returning Player[] in old versions of spigot
     */
    public static Collection<? extends Player> getOnlinePlayers() {
        try {
            return Bukkit.getOnlinePlayers();
        } catch (NoSuchMethodError ignored) {}

        if (GET_ONLINE_PLAYERS != null) {
            try {
                final Object result = GET_ONLINE_PLAYERS.invoke(null);

                if (result != null && result instanceof Player[]) {
                    return Arrays.asList((Player[]) result);
                }
            } catch (Exception ignored) {}
        }

        return Collections.emptyList();
    }
}
