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

import me.realized._duels.commands.SubCommand;
import me.realized._duels.utilities.Helper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ListCommand extends SubCommand {

    public ListCommand() {
        super("list", "list", "duels.admin", "Displays the lobby location and lists arenas and kits.", 1);
    }

    @Override
    public void execute(Player sender, String[] args) {
        String arenas = Helper.join(arenaManager.getArenaNames(), "&r, ");
        String kits = Helper.join(kitManager.getKitNames(), ", ");
        String lobby = (dataManager.getLobby().equals(Bukkit.getWorlds().get(0).getSpawnLocation()) ? "lobby not set, using default world spawn location"
            : Helper.format(dataManager.getLobby()));
        Helper.pm(sender, "Extra.list", true, "{ARENAS}", arenas, "{KITS}", kits, "{LOBBY}", lobby);
    }
}
