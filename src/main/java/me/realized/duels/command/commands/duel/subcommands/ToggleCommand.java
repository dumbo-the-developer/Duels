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

package me.realized.duels.command.commands.duel.subcommands;

import java.util.Optional;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.data.UserData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleCommand extends BaseCommand {

    public ToggleCommand(final DuelsPlugin plugin) {
        super(plugin, "toggle", "toggle", "Toggle your duel requests.", "duels.toggle", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Optional<UserData> cached = userManager.get((Player) sender);

        if (!cached.isPresent()) {
            lang.sendMessage(sender, "ERROR.data-load-failure");
            return;
        }

        final UserData data = cached.get();
        data.setRequests(!data.canRequest());
        lang.sendMessage(sender, "COMMAND.duel.toggle." + (data.canRequest() ? "enabled" : "disabled"));
    }
}
