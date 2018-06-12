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

package me.realized.duels.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.data.UserData;
import me.realized.duels.data.UserManager;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

public class MVdWPlaceholderHook extends PluginHook<DuelsPlugin> {

    private final UserManager userDataManager;

    public MVdWPlaceholderHook(final DuelsPlugin plugin) {
        super(plugin, "MVdWPlaceholderAPI");
        this.userDataManager = plugin.getUserManager();

        final Placeholders placeholders = new Placeholders();
        PlaceholderAPI.registerPlaceholder(plugin, "duels_wins", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "duels_losses", placeholders);
    }

    public class Placeholders implements PlaceholderReplacer {

        @Override
        public String onPlaceholderReplace(final PlaceholderReplaceEvent event) {
            final Player player = event.getPlayer();

            if (player == null) {
                return "Player is required";
            }

            final UserData user = userDataManager.get(player);

            if (user == null) {
                return null;
            }

            return String.valueOf(event.getPlaceholder().equals("duels_wins") ? user.getWins() : user.getLosses());
        }
    }
}
