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

package me.realized._duels.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import me.realized._duels.Core;
import me.realized._duels.configuration.MainConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EssentialsHook extends PluginHook {

    private final MainConfig config;

    public EssentialsHook(Core instance) {
        super("Essentials");
        this.config = instance.getConfiguration();
    }

    public void setUnvanished(Player player) {
        if (!config.isPatchesToggleVanishOnStart() || !isEnabled()) {
            return;
        }

        Essentials essentials = (Essentials) getPlugin();
        User user = essentials.getUser(player);

        if (user != null) {
            user.setVanished(false);
            user.setVanished(true);
            user.setVanished(false);
        }
    }

    public void setBackLocation(Player player, Location location) {
        if (!config.isPatchesSetBackLocation() || !isEnabled()) {
            return;
        }

        Essentials essentials = (Essentials) getPlugin();
        User user = essentials.getUser(player);

        if (user != null) {
            user.setLastLocation(location);
        }
    }
}
