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

package me.realized._duels.commands.duel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import me.realized._duels.commands.BaseCommand;
import me.realized._duels.commands.SubCommand;
import me.realized._duels.commands.duel.subcommands.AcceptCommand;
import me.realized._duels.commands.duel.subcommands.DenyCommand;
import me.realized._duels.commands.duel.subcommands.StatsCommand;
import me.realized._duels.commands.duel.subcommands.ToggleCommand;
import me.realized._duels.data.UserData;
import me.realized._duels.dueling.RequestManager;
import me.realized._duels.dueling.Settings;
import me.realized._duels.event.RequestSendEvent;
import me.realized._duels.hooks.CombatTagPlusHook;
import me.realized._duels.hooks.WorldGuardHook;
import me.realized._duels.utilities.Helper;
import me.realized._duels.utilities.Storage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DuelCommand extends BaseCommand {

    private static final Set<String> DEFAULT_CHILDREN = new HashSet<>();
    private static final Set<String> VALIDATION_BYPASSED_CHILDREN = new HashSet<>();
    private final WorldGuardHook wgHook;
    private final CombatTagPlusHook ctHook;
    private final Map<String, SubCommand> children = new HashMap<>();

    public DuelCommand() {
        super("duel", "duels.duel");
        this.wgHook = (WorldGuardHook) hookManager.get("WorldGuard");
        this.ctHook = (CombatTagPlusHook) hookManager.get("CombatTagPlus");

        children.put("accept", new AcceptCommand());
        children.put("deny", new DenyCommand());
        children.put("toggle", new ToggleCommand());
        children.put("stats", new StatsCommand());
        DEFAULT_CHILDREN.addAll(children.keySet());
        VALIDATION_BYPASSED_CHILDREN.addAll(Arrays.asList("stats", "toggle"));
    }

    @Override
    public void execute(Player sender, String[] args) {
        if (args.length == 0) {
            Helper.pm(sender, "Commands.duel.usage", true);
            return;
        }

        if (!VALIDATION_BYPASSED_CHILDREN.contains(args[0].toLowerCase())) {
            if (!config.isDuelingUseOwnInventory() && config.isDuelingRequiresClearedInventory() && !Helper.hasEmptyInventory(sender)) {
                Helper.pm(sender, "Errors.empty-inventory-only", true);
                return;
            }

            if (ctHook.isTagged(sender)) {
                Helper.pm(sender, "Errors.is-combat-tagged", true);
                return;
            }

            if (!wgHook.canUseDuelCommands(sender)) {
                Helper.pm(sender, "Errors.not-in-duelzone", true, "{REGION}", Helper.join(config.getDuelZoneRegions(), ", "));
                return;
            }
        }

        SubCommand child = children.get(args[0].toLowerCase());

        if (child != null) {
            if (Bukkit.getPlayer(args[0]) != null) {
                Helper.pm(sender, "&aWere you attempting to duel a player named '" + args[0] + "'? If so, type '&2/duel -" + args[0] + "&a' instead.", false);
            }

            if (!sender.hasPermission(child.getPermission())) {
                Helper.pm(sender, "Errors.no-permission", true);
                return;
            }

            if (args.length < child.length()) {
                Helper.pm(sender, "Commands.duel.sub-command-usage", true, "{USAGE}", "/" + getName() + " " + child.getUsage(), "{DESCRIPTION}", child.getDescription());
                return;
            }

            child.execute(sender, args);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0].replace("-", ""));

        if (target == null) {
            Helper.pm(sender, "Errors.player-not-found", true);
            return;
        }

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            Helper.pm(sender, "Errors.cannot-duel-yourself", true);
            return;
        }

        UserData data = dataManager.getUser(target.getUniqueId(), false);

        if (data == null) {
            Helper.pm(sender, "Errors.player-not-found", true);
            return;
        }

        if (!data.canRequest()) {
            Helper.pm(sender, "Errors.has-requests-disabled", true);
            return;
        }

        if (spectatorManager.isSpectating(sender)) {
            Helper.pm(sender, "Errors.is-in-spectator-mode.sender", true);
            return;
        }

        if (spectatorManager.isSpectating(target)) {
            Helper.pm(sender, "Errors.is-in-spectator-mode.target", true);
            return;
        }

        if (arenaManager.isInMatch(sender)) {
            Helper.pm(sender, "Errors.already-in-match.sender", true);
            return;
        }

        if (arenaManager.isInMatch(target)) {
            Helper.pm(sender, "Errors.already-in-match.target", true);
            return;
        }

        if (requestManager.hasRequestTo(sender, target) == RequestManager.Result.FOUND) {
            Helper.pm(sender, "Errors.already-requested", true, "{PLAYER}", target.getName());
            return;
        }

        if (!config.isDuelingUseOwnInventory()) {
            Storage.get(sender).set("request", new Settings(target.getUniqueId(), sender.getLocation().clone()));
            sender.openInventory(kitManager.getGUI().getFirst());
        } else {
            requestManager.sendRequestTo(sender, target, new Settings(target.getUniqueId(), sender.getLocation().clone()));
            Helper.pm(sender, "Dueling.on-request-send.sender", true, "{PLAYER}", target.getName(), "{KIT}", "none", "{ARENA}", "random");
            Helper.pm(target, "Dueling.on-request-send.receiver", true, "{PLAYER}", sender.getName(), "{KIT}", "none", "{ARENA}", "random");

            RequestSendEvent requestSendEvent = new RequestSendEvent(requestManager.getRequestTo(sender, target), sender, target);
            Bukkit.getPluginManager().callEvent(requestSendEvent);
        }
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
