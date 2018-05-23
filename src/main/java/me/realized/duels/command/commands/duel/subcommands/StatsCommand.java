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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand extends BaseCommand {

    public StatsCommand(final DuelsPlugin plugin) {
        super(plugin, "stats", null, null, "Displays your duel stats.", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length > getLength()) {
            final Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null) {
                // boi
                return;
            }

            displayStats(sender, target);
        }

        displayStats(sender, (Player) sender);
    }

    private void displayStats(final CommandSender sender, final Player player) {
        final Optional<UserData> cached = userManager.get(player);

        if (!cached.isPresent()) {
            // boi
            return;
        }

        final UserData data = cached.get();
        sender.sendMessage("Stats of " + data.getName() + " -");
        sender.sendMessage("Wins: " + data.getWins());
        sender.sendMessage("Losses: " + data.getLosses());
        sender.sendMessage("W/L Ratio: " + (double) data.getWins() / (double) data.getLosses());
    }
}
