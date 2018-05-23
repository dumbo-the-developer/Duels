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

package me.realized._duels.commands;

import me.realized._duels.Core;
import me.realized._duels.arena.ArenaManager;
import me.realized._duels.configuration.ConfigType;
import me.realized._duels.configuration.MainConfig;
import me.realized._duels.configuration.MessagesConfig;
import me.realized._duels.data.DataManager;
import me.realized._duels.dueling.RequestManager;
import me.realized._duels.dueling.SpectatorManager;
import me.realized._duels.hooks.HookManager;
import me.realized._duels.kits.KitManager;
import me.realized._duels.utilities.Helper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class BaseCommand implements CommandExecutor {

    protected final MainConfig config = Core.getInstance().getConfiguration();
    protected final MessagesConfig messages = (MessagesConfig) Core.getInstance().getConfigManager().getConfigByType(ConfigType.MESSAGES);
    protected final RequestManager requestManager = Core.getInstance().getRequestManager();
    protected final DataManager dataManager = Core.getInstance().getDataManager();
    protected final ArenaManager arenaManager = Core.getInstance().getArenaManager();
    protected final SpectatorManager spectatorManager = Core.getInstance().getSpectatorManager();
    protected final KitManager kitManager = Core.getInstance().getKitManager();
    protected final HookManager hookManager = Core.getInstance().getHookManager();
    private final String command;
    private final String permission;

    protected BaseCommand(String command, String permission) {
        this.command = command;
        this.permission = permission;
    }

    public String getName() {
        return command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Helper.pm(sender, "&cThis command cannot be ran by " + sender.getName() + ".", false);
            return true;
        }

        if (!sender.hasPermission(permission)) {
            Helper.pm(sender, "Errors.no-permission", true);
            return true;
        }

        execute((Player) sender, args);
        return true;
    }

    public abstract void execute(Player sender, String[] args);
}
