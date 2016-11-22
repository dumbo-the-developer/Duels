package me.realized.duels.commands.duel;

import me.realized.duels.commands.BaseCommand;
import me.realized.duels.commands.SubCommand;
import me.realized.duels.commands.duel.subcommands.AcceptCommand;
import me.realized.duels.commands.duel.subcommands.DenyCommand;
import me.realized.duels.commands.duel.subcommands.StatsCommand;
import me.realized.duels.commands.duel.subcommands.ToggleCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.dueling.RequestManager;
import me.realized.duels.dueling.Settings;
import me.realized.duels.event.RequestSendEvent;
import me.realized.duels.hooks.WorldGuardHook;
import me.realized.duels.utilities.Helper;
import me.realized.duels.utilities.Storage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DuelCommand extends BaseCommand {

    private final WorldGuardHook wgHook;
    private final Map<String, SubCommand> children = new HashMap<>();

    public DuelCommand() {
        super("duel", "duels.duel");
        this.wgHook = (WorldGuardHook) hookManager.get("WorldGuard");

        children.put("accept", new AcceptCommand());
        children.put("deny", new DenyCommand());
        children.put("toggle", new ToggleCommand());
        children.put("stats", new StatsCommand());
    }

    @Override
    public void execute(Player sender, String[] args) {
        if (args.length == 0) {
            Helper.pm(sender, "Commands.duel.usage", true);
            return;
        }

        if (!config.isDuelingUseOwnInventory() && config.isDuelingRequiresClearedInventory() && !Helper.hasEmptyInventory(sender)) {
            Helper.pm(sender, "Errors.empty-inventory-only", true);
            return;
        }

        if (!wgHook.canUseDuelCommands(sender)) {
            Helper.pm(sender, "Errors.not-in-duelzone", true, "{REGION}", Helper.join(config.getDuelZoneRegions(), ", "));
           return;
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
}
