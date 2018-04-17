package me.realized.duels.command.commands.duel;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duel.subcommands.StatsCommand;
import me.realized.duels.gui.setting.SettingGui;
import me.realized.duels.util.gui.SinglePageGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand extends BaseCommand {

    public DuelCommand(final DuelsPlugin plugin) {
        super(plugin, "duel", "duels.duel", true);
        child(new StatsCommand(plugin));
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

//        final Player target = Bukkit.getPlayerExact(args[0]);
//
//        if (target == null) {
//            sender.sendMessage("Player not found");
//            return true;
//        }

        final Player player = (Player) sender;
        final Setting setting = settingCache.get(player);

        if (setting.getTargetName() != null && !setting.getTargetName().equals(args[0])) {
            setting.reset();
        }

        setting.setTargetName(args[0]);

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

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return null;
    }
}
