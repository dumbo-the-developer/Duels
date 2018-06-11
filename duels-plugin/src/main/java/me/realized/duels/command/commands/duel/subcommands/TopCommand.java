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

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.user.UserManager.SortedEntry;
import me.realized.duels.command.BaseCommand;
import org.bukkit.command.CommandSender;

public class TopCommand extends BaseCommand {

    public TopCommand(final DuelsPlugin plugin) {
        super(plugin, "top", "top [wins|losses]", "Displays top duel wins & losses.", "duels.top", 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final boolean wins = args[1].equalsIgnoreCase("wins");
        final List<SortedEntry<String, Integer>> top = wins ? userManager.getTopWins() : userManager.getTopLosses();

        if (top == null || top.isEmpty()) {
            lang.sendMessage(sender, "ERROR.no-data-available");
            return;
        }

        lang.sendMessage(sender, "COMMAND.duel.top.next-update",
            "remaining", wins ? userManager.getNextWinsUpdate() : userManager.getNextLossesUpdate());
        lang.sendMessage(sender, "COMMAND.duel.top.header", "type", wins ? "Wins" : "Losses");

        for (int i = 0; i < top.size(); i++) {
            final SortedEntry<String, Integer> entry = top.get(i);
            lang.sendMessage(sender, "COMMAND.duel.top.display-format",
                "rank", i + 1, "name", entry.getKey(), "score", entry.getValue(), "type", wins ? "Wins" : "Losses");
        }

        lang.sendMessage(sender, "COMMAND.duel.top.footer", "type", wins ? "Wins" : "Losses");
    }
}
