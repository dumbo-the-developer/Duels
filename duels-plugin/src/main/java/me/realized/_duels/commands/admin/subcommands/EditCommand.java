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

public class EditCommand extends SubCommand {

    public EditCommand() {
        super("edit", "edit [player] [add:remove:set] [wins:losses] [quantity]", "duels.admin", "Edit player's stats.", 5);
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

        if (!UserData.EditType.isValue(args[2].toUpperCase())) {
            Helper.pm(sender, "Errors.edit-failed", true, "{REASON}", args[2] + " is not a valid edit type. You may choose from one of the following: add, remove, set");
            return;
        }

        UserData.EditType editType = UserData.EditType.valueOf(args[2].toUpperCase());

        if (!UserData.StatsType.isValue(args[3].toUpperCase())) {
            Helper.pm(sender, "Errors.edit-failed", true, "{REASON}", args[3] + " is not a valid stats type. You may choose from one of the following: wins, losses");
            return;
        }

        UserData.StatsType statsType = UserData.StatsType.valueOf(args[3].toUpperCase());

        if (!Helper.isInt(args[4], false)) {
            Helper.pm(sender, "Errors.edit-failed", true, "{REASON}", args[4] + " is not a valid amount.");
            return;
        }

        int amount = Integer.parseInt(args[4]);

        target.edit(editType, statsType, amount);
        String action = editType.name().toLowerCase() + " " + amount + " " + statsType.name().toLowerCase();
        Helper.pm(sender, "Stats.edit", true, "{PLAYER}", target.getName(), "{ACTION}", action);
    }
}
