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

package me.realized.duels.api;

import java.util.UUID;
import me.realized._duels.Core;
import me.realized._duels.data.UserData;
import org.bukkit.entity.Player;

/**
 * @author Realized
 * @deprecated As of v3.0.0. Use {@link Duels} instead.
 *
 * A static API for Duels.
 */

@Deprecated
public class DuelsAPI {

    private static final Core instance = Core.getInstance();

    /**
     * @param uuid - UUID of the player to get userdata.
     * @param force - Should we force the load from the files?
     * @return UserData of the player if exists or null.
     */
    @Deprecated
    public static UserData getUser(UUID uuid, boolean force) {
        return instance.getDataManager().getUser(uuid, force);
    }

    /**
     * @param player - player to get userdata.
     * @param force - Force the load from the files if not in cache?
     * @return UserData of the player if exists or null.
     */
    @Deprecated
    public static UserData getUser(Player player, boolean force) {
        return instance.getDataManager().getUser(player.getUniqueId(), force);
    }

    /**
     * @param player - player to check if in match.
     * @return true if player is in match, false otherwise.
     */
    @Deprecated
    public static boolean isInMatch(Player player) {
        return instance.getArenaManager().isInMatch(player);
    }

    /**
     * @return version string of the plugin.
     */
    @Deprecated
    public static String getVersion() {
        return instance.getDescription().getVersion();
    }
}
