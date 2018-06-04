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

import java.util.UUID;
import me.realized._duels.commands.SubCommand;
import me.realized._duels.data.UserData;
import me.realized._duels.utilities.Helper;
import org.bukkit.entity.Player;

public class ResetCommand extends SubCommand {

    public ResetCommand() {
        super("reset", "reset [player]", "duels.admin", "Completely reset a player's stats.", 2);
    }

    @Override
    public void execute(Player sender, String[] args) {
        UUID uuid = Helper.getUUID(args[1]);

        if (uuid == null) {
            Helper.pm(sender, "Errors.player-not-found", true);
            return;
        }

        UserData target = dataManager.getUser(uuid, true);

        if (target == null) {
            Helper.pm(sender, "Errors.player-not-found", true);
            return;
        }

        target.edit(UserData.EditType.SET, UserData.StatsType.WINS, 0);
        target.edit(UserData.EditType.SET, UserData.StatsType.LOSSES, 0);
        target.getMatches().clear();
        Helper.pm(sender, "Stats.reset", true, "{PLAYER}", target.getName());
    }
}
