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
import me.realized._duels.dueling.DuelManager;
import me.realized._duels.dueling.RequestManager;
import me.realized._duels.hooks.HookManager;
import me.realized._duels.kits.KitManager;
import org.bukkit.entity.Player;

public abstract class SubCommand {

    protected final MainConfig config = Core.getInstance().getConfiguration();
    protected final MessagesConfig messages = (MessagesConfig) Core.getInstance().getConfigManager().getConfigByType(ConfigType.MESSAGES);
    protected final HookManager hookManager = Core.getInstance().getHookManager();
    protected final RequestManager requestManager = Core.getInstance().getRequestManager();
    protected final DataManager dataManager = Core.getInstance().getDataManager();
    protected final ArenaManager arenaManager = Core.getInstance().getArenaManager();
    protected final KitManager kitManager = Core.getInstance().getKitManager();
    protected final DuelManager duelManager = Core.getInstance().getDuelManager();
    private final String name;
    private final String usage;
    private final String permission;
    private final String description;
    private final int length;

    protected SubCommand(String name, String usage, String permission, String description, int length) {
        this.name = name;
        this.usage = usage;
        this.permission = permission;
        this.description = description;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    public int length() {
        return length;
    }

    public abstract void execute(Player sender, String[] args);
}
