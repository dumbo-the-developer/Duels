package me.realized.duels.command.commands.duel;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duel.subcommands.AcceptCommand;
import me.realized.duels.command.commands.duel.subcommands.DenyCommand;
import me.realized.duels.command.commands.duel.subcommands.StatsCommand;
import me.realized.duels.gui.setting.SettingGui;
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
            sender.sendMessage("/duel [name]");
            return true;
        }

        if (isChild(args[0])) {
            return false;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            sender.sendMessage("Player not found");
            return true;
        }

        final Player player = (Player) sender;

        if (requestManager.has(player, target)) {
            player.sendMessage("You already have a request sent to " + target.getName());
            return true;
        }

        final Setting setting = settingCache.get(player);

        if (setting.getTarget() != null && !setting.getTarget().equals(target.getUniqueId())) {
            setting.reset();
        }

        setting.setTarget(target.getUniqueId());

        SettingGui gui = setting.getGui();

        if (gui == null) {
            gui = new SettingGui(plugin);
            guiListener.addGui(player, gui);
            setting.setGui(gui);
        }

        gui.open(player);
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
