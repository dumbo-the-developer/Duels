package me.realized.duels.command.commands.duel;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duel.subcommands.AcceptCommand;
import me.realized.duels.command.commands.duel.subcommands.DenyCommand;
import me.realized.duels.command.commands.duel.subcommands.StatsCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand extends BaseCommand {

    public DuelCommand(final DuelsPlugin plugin) {
        super(plugin, "duel", "duels.duel", true);
        child(new StatsCommand(plugin), new AcceptCommand(plugin), new DenyCommand(plugin));
    }

    @Override
    protected boolean executeFirst(final CommandSender sender, final String label, final String[] args) {
        if (args.length == 0) {
            lang.sendMessage(sender, "COMMAND.duel.usage");
            return true;
        }

        // Proceed to child command execution if given argument is a name of a child command
        if (isChild(args[0])) {
            return false;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            lang.sendMessage(sender, "ERROR.player-not-found", "name", args[0]);
            return true;
        }

        final Player player = (Player) sender;

        if (player.equals(target)) {
            lang.sendMessage(sender, "ERROR.target-is-self");
            return true;
        }

        if (requestManager.has(player, target)) {
            lang.sendMessage(sender, "ERROR.already-has-request", "player", target.getName());
            return true;
        }

        final Setting setting = settingCache.get(player);
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
