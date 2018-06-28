package me.realized.duels.command.commands.duel;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duel.subcommands.AcceptCommand;
import me.realized.duels.command.commands.duel.subcommands.DenyCommand;
import me.realized.duels.command.commands.duel.subcommands.InventoryCommand;
import me.realized.duels.command.commands.duel.subcommands.StatsCommand;
import me.realized.duels.command.commands.duel.subcommands.ToggleCommand;
import me.realized.duels.command.commands.duel.subcommands.TopCommand;
import me.realized.duels.command.commands.duel.subcommands.VersionCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.extra.Permissions;
import me.realized.duels.hooks.VaultHook;
import me.realized.duels.hooks.WorldGuardHook;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand extends BaseCommand {

    private final WorldGuardHook worldGuard;
    private final VaultHook vault;

    public DuelCommand(final DuelsPlugin plugin) {
        super(plugin, "duel", "duels.duel", true);
        child(
            new AcceptCommand(plugin),
            new DenyCommand(plugin),
            new StatsCommand(plugin),
            new ToggleCommand(plugin),
            new TopCommand(plugin),
            new InventoryCommand(plugin),
            new VersionCommand(plugin)
        );
        this.worldGuard = hookManager.getHook(WorldGuardHook.class);
        this.vault = hookManager.getHook(VaultHook.class);
    }

    @Override
    protected boolean executeFirst(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (userManager.get(player) == null) {
            lang.sendMessage(sender, "ERROR.data.load-failure");
            return true;
        }

        if (args.length == 0) {
            lang.sendMessage(sender, "COMMAND.duel.usage");
            return true;
        }

        if (isChild(args[0])) {
            return false;
        }

        if (config.isRequiresClearedInventory() && InventoryUtil.hasItem(player)) {
            lang.sendMessage(sender, "ERROR.duel.inventory-not-empty");
            return true;
        }

        if (config.isPreventCreativeMode() && player.getGameMode() == GameMode.CREATIVE) {
            lang.sendMessage(sender, "ERROR.duel.in-creative-mode");
            return true;
        }

        if (config.isDuelZoneEnabled() && worldGuard != null && !worldGuard.inDuelZone(player)) {
            lang.sendMessage(sender, "ERROR.duel.not-in-duelzone", "regions", config.getDuelZoneRegions());
            return true;
        }

        if (arenaManager.isInMatch(player)) {
            lang.sendMessage(sender, "ERROR.duel.already-in-match.sender");
            return true;
        }

        if (spectateManager.isSpectating(player)) {
            lang.sendMessage(sender, "ERROR.spectate.already-spectating.sender");
            return true;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[0]);
            return true;
        }

        if (player.equals(target)) {
            lang.sendMessage(sender, "ERROR.duel.is-self");
            return true;
        }

        final UserData user = userManager.get(target);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "name", target.getName());
            return true;
        }

        if (!sender.hasPermission(Permissions.ADMIN) && !user.canRequest()) {
            lang.sendMessage(sender, "ERROR.duel.requests-disabled", "name", target.getName());
            return true;
        }

        if (requestManager.has(player, target)) {
            lang.sendMessage(sender, "ERROR.duel.already-has-request", "name", target.getName());
            return true;
        }

        if (arenaManager.isInMatch(target)) {
            lang.sendMessage(sender, "ERROR.duel.already-in-match.target", "name", target.getName());
            return true;
        }

        if (spectateManager.isSpectating(target)) {
            lang.sendMessage(sender, "ERROR.spectate.already-spectating.target", "name", target.getName());
            return true;
        }

        final Settings settings = settingManager.getSafely(player);
        settings.setBet(0);

        if (config.isMoneyBettingEnabled() && args.length > 1) {
            if (config.isMoneyBettingUsePermission() && !player.hasPermission(Permissions.MONEY_BETTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
                lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.MONEY_BETTING);
                return true;
            }

            final int amount = NumberUtil.parseInt(args[1]).orElse(0);

            if (vault == null || vault.getEconomy() == null ) {
                lang.sendMessage(sender, "ERROR.setting.disabled-option", "option", "Betting");
                return true;
            }

            if (!vault.getEconomy().has(player, amount)) {
                lang.sendMessage(sender, "ERROR.command.not-enough-money");
                return true;
            }

            settings.setBet(amount);
        }

        settings.setTarget(target);

        if (config.isUseOwnInventoryEnabled()) {
            settings.openGui(player);
        } else {
            kitManager.getGui().open(player);
        }
        return true;
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {}

    // Disables default TabCompleter
    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return null;
    }
}
