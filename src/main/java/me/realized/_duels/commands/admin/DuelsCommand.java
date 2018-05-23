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

package me.realized._duels.commands.admin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import me.realized._duels.commands.BaseCommand;
import me.realized._duels.commands.SubCommand;
import me.realized._duels.commands.admin.subcommands.CreateCommand;
import me.realized._duels.commands.admin.subcommands.DeleteCommand;
import me.realized._duels.commands.admin.subcommands.DeletekitCommand;
import me.realized._duels.commands.admin.subcommands.EditCommand;
import me.realized._duels.commands.admin.subcommands.InfoCommand;
import me.realized._duels.commands.admin.subcommands.ListCommand;
import me.realized._duels.commands.admin.subcommands.LoadkitCommand;
import me.realized._duels.commands.admin.subcommands.PlaysoundCommand;
import me.realized._duels.commands.admin.subcommands.ReloadCommand;
import me.realized._duels.commands.admin.subcommands.ResetCommand;
import me.realized._duels.commands.admin.subcommands.SavekitCommand;
import me.realized._duels.commands.admin.subcommands.SetCommand;
import me.realized._duels.commands.admin.subcommands.SetitemCommand;
import me.realized._duels.commands.admin.subcommands.SetlobbyCommand;
import me.realized._duels.commands.admin.subcommands.ToggleCommand;
import me.realized._duels.utilities.Helper;
import org.bukkit.entity.Player;

public class DuelsCommand extends BaseCommand {

    private static final Set<String> DEFAULT_CHILDREN = new HashSet<>();
    private final Map<String, SubCommand> children = new HashMap<>();

    public DuelsCommand() {
        super("duels", "duels.admin");

        children.put("create", new CreateCommand());
        children.put("delete", new DeleteCommand());
        children.put("edit", new EditCommand());
        children.put("info", new InfoCommand());
        children.put("list", new ListCommand());
        children.put("loadkit", new LoadkitCommand());
        children.put("savekit", new SavekitCommand());
        children.put("deletekit", new DeletekitCommand());
        children.put("reset", new ResetCommand());
        children.put("set", new SetCommand());
        children.put("setitem", new SetitemCommand());
        children.put("setlobby", new SetlobbyCommand());
        children.put("toggle", new ToggleCommand());
        children.put("playsound", new PlaysoundCommand());
        children.put("reload", new ReloadCommand());
        DEFAULT_CHILDREN.addAll(children.keySet());
    }

    @Override
    public void execute(Player sender, String[] args) {
        if (args.length == 0) {
            Helper.pm(sender, "Commands.duels.usage", true);
            return;
        }

        SubCommand child = children.get(args[0].toLowerCase());

        if (child == null) {
            Helper.pm(sender, "Errors.invalid-sub-command", true, "{ARGUMENT}", args[0]);
            return;
        }

        if (!sender.hasPermission(child.getPermission())) {
            Helper.pm(sender, "Errors.no-permission", true);
            return;
        }

        if (args.length < child.length()) {
            Helper.pm(sender, "Commands.duels.sub-command-usage", true, "{USAGE}", "/" + getName() + " " + child.getUsage(), "{DESCRIPTION}", child.getDescription());
            return;
        }

        child.execute(sender, args);
    }

    public boolean registerChildren(SubCommand command) {
        if (children.get(command.getName()) != null) {
            return false;
        }

        children.put(command.getName(), command);
        return true;
    }

    public boolean unregisterChildren(String name) {
        return !DEFAULT_CHILDREN.contains(name) && children.remove(name) != null;

    }
}
