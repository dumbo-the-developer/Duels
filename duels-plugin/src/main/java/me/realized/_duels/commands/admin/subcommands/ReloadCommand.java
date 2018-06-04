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

package me.realized._duels.commands.admin.subcommands;

import me.realized._duels.Core;
import me.realized._duels.commands.SubCommand;
import me.realized._duels.utilities.Helper;
import me.realized._duels.utilities.Reloadable;
import org.bukkit.entity.Player;

public class ReloadCommand extends SubCommand {

    public ReloadCommand() {
        super("reload", "reload [weak]", "duels.admin", "Reloads the plugin completely or only the messages. Append 'weak' to only reload messages file.", 1);
    }

    @Override
    public void execute(Player sender, String[] args) {
        Core instance = Core.getInstance();

        if (args.length > 1 && args[1].equalsIgnoreCase("weak")) {
            instance.reload(Reloadable.ReloadType.WEAK);
            Helper.pm(sender, "&a" + instance.getDescription().getFullName() + ": Weak reload complete.", false);
            return;
        }

        instance.reload(Reloadable.ReloadType.STRONG);
        Helper.pm(sender, "&a" + instance.getDescription().getFullName() + ": Reload complete.", false);
    }
}
