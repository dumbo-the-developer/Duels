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

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import java.util.Arrays;
import java.util.List;
import me.realized._duels.Core;
import me.realized._duels.data.UserData;
import org.bukkit.entity.Player;

public class MVdWPlaceholderHook extends PluginHook implements PlaceholderReplacer {

    private static final List<String> PLACEHOLDERS = Arrays.asList("duels_wins", "duels_losses", "duels_request_enabled");

    private final Core instance;

    public MVdWPlaceholderHook(Core instance) {
        super("MVdWPlaceholderAPI");
        this.instance = instance;

        if (isEnabled()) {
            for (String placeholder : PLACEHOLDERS) {
                PlaceholderAPI.registerPlaceholder(instance, placeholder, this);
            }
        }
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
        Player player = event.getPlayer();

        if (player == null) {
            return "Player is required.";
        }

        UserData user = instance.getDataManager().getUser(player.getUniqueId(), false);

        if (user == null) {
            return "User data not found.";
        }

        switch (event.getPlaceholder()) {
            case "duels_wins":
                return String.valueOf(user.get(UserData.StatsType.WINS));
            case "duels_losses":
                return String.valueOf(user.get(UserData.StatsType.LOSSES));
            case "duels_request_enabled":
                return user.canRequest() ? "enabled" : "disabled";
        }

        return "Invalid placeholder.";
    }
}
