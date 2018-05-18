package me.realized.duels.command.commands.duel;

import java.util.List;
import java.util.OptionalInt;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duel.subcommands.AcceptCommand;
import me.realized.duels.command.commands.duel.subcommands.DenyCommand;
import me.realized.duels.command.commands.duel.subcommands.StatsCommand;
import me.realized.duels.command.commands.duel.subcommands.ToggleCommand;
import me.realized.duels.hooks.VaultHook;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand extends BaseCommand {

    private final VaultHook vaultHook;

    public DuelCommand(final DuelsPlugin plugin) {
        super(plugin, "duel", "duels.duel", true);
        child(new AcceptCommand(plugin), new DenyCommand(plugin), new StatsCommand(plugin), new ToggleCommand(plugin));
        this.vaultHook = hookManager.getHook(VaultHook.class).orElse(null);
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

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
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

        final Setting setting = settingCache.getSafely(player);

        if (config.isAllowMoneyBetting() && args.length > 1) {
            if (vaultHook == null || !vaultHook.hasEconomy()) {
                sender.sendMessage(ChatColor.RED + "Betting is currently disabled.");
                return true;
            }

            final OptionalInt amount = NumberUtil.parseInt(args[1]);

            if (!amount.isPresent()) {
                sender.sendMessage(ChatColor.RED + "Invalid amount!");
                return true;
            }

            if (!vaultHook.getEconomy().has(player, amount.getAsInt())) {
                sender.sendMessage(ChatColor.RED + "You do not have enough to bet!");
                return true;
            }

            setting.setBet(amount.getAsInt());
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
