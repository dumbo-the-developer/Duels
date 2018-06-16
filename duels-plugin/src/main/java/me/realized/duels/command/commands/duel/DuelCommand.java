package me.realized.duels.command.commands.duel;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duel.subcommands.AcceptCommand;
import me.realized.duels.command.commands.duel.subcommands.DenyCommand;
import me.realized.duels.command.commands.duel.subcommands.StatsCommand;
import me.realized.duels.command.commands.duel.subcommands.ToggleCommand;
import me.realized.duels.command.commands.duel.subcommands.TopCommand;
import me.realized.duels.extra.Permissions;
import me.realized.duels.hooks.VaultHook;
import me.realized.duels.setting.Setting;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand extends BaseCommand {

    private final VaultHook vault;

    public DuelCommand(final DuelsPlugin plugin) {
        super(plugin, "duel", "duels.duel", true);
        child(new AcceptCommand(plugin), new DenyCommand(plugin), new StatsCommand(plugin), new ToggleCommand(plugin), new TopCommand(plugin));
        this.vault = hookManager.getHook(VaultHook.class);
    }

    @Override
    protected boolean executeFirst(final CommandSender sender, final String label, final String[] args) {
        if (args.length == 0) {
            lang.sendMessage(sender, "COMMAND.duel.usage");
            return true;
        }

        if (isChild(args[0])) {
            return false;
        }

        final Player player = (Player) sender;

        if (config.isRequiresClearedInventory() && InventoryUtil.hasItem(player)) {
            lang.sendMessage(sender, "ERROR.inventory-not-empty");
            return true;
        }

        if (config.isPreventCreativeMode() && player.getGameMode() == GameMode.CREATIVE) {
            // TODO: 16/06/2018 send msg
            return true;
        }

        if (arenaManager.isInMatch(player)) {
            lang.sendMessage(sender, "ERROR.already-in-match.sender");
            return true;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player-not-found", "name", args[0]);
            return true;
        }

//        if (player.equals(target)) {
//            lang.sendMessage(sender, "ERROR.target-is-self");
//            return true;
//        }

        if (requestManager.has(player, target)) {
            lang.sendMessage(sender, "ERROR.already-has-request", "player", target.getName());
            return true;
        }

        if (arenaManager.isInMatch(target)) {
            lang.sendMessage(sender, "ERROR.already-in-match.target", "player", target.getName());
            return true;
        }

        final Setting setting = settingManager.getSafely(player);

        if (config.isMoneyBettingEnabled() && args.length > 1) {
            if (config.isMoneyBettingUsePermission() && !player.hasPermission(Permissions.MONEY_BETTING)) {
                lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.MONEY_BETTING);
                return true;
            }

            final int amount = NumberUtil.parseInt(args[1]).orElse(0);

            if (vault == null || vault.getEconomy() == null) {
                sender.sendMessage(ChatColor.RED + "Betting is currently disabled.");
                return true;
            }

            if (!vault.getEconomy().has(player, amount)) {
                sender.sendMessage(ChatColor.RED + "You do not have enough money to bet!");
                return true;
            }

            setting.setBet(amount);
        }

        setting.setTarget(target);
        setting.openGui(player);
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
