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

package me.realized.duels.command.commands.duels.subcommands;

import java.util.Optional;
import java.util.OptionalInt;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.util.NumberUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetCommand extends BaseCommand {

    public SetCommand(final DuelsPlugin plugin) {
        super(plugin, "set", "set [name] [1 | 2]", "Sets the teleport location of an arena.", null, 3, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Optional<Arena> result = arenaManager.get(StringUtils.join(args, " ", 1, args.length - 1));

        if (!result.isPresent()) {
            // send msg
            return;
        }

        final OptionalInt pos = NumberUtil.parseInt(args[args.length - 1]);

        if (!pos.isPresent()) {
            // send msg
            return;
        }

        final Arena arena = result.get();
        arena.setPosition(pos.getAsInt(), ((Player) sender).getLocation().clone());
        sender.sendMessage("Set pos " + pos.getAsInt() + " for arena '" + arena.getName() + "'!");
    }
}
