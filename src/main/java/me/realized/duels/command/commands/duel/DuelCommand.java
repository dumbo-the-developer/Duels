package me.realized.duels.command.commands.duel;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duel.subcommands.StatsCommand;
import me.realized.duels.gui.setting.RequestDetailsButton;
import me.realized.duels.util.gui.SinglePageGUI;
import me.realized.duels.util.inventory.InventoryUtil;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
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

        final Setting setting = settingCache.get((Player) sender);
        setting.setTargetName(args[0]);

        SinglePageGUI gui = setting.getSettingGui();

        if (gui == null) {
            gui = new SinglePageGUI("Request Settings", 4);
            InventoryUtil.fillRange(gui.getInventory(), 0, 9, ItemBuilder.of(Material.STAINED_GLASS_PANE).name(" ").build(), false);
            gui.add(4, new RequestDetailsButton(settingCache));
        }

        gui.open((Player) sender);
        return true;
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {}

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return null;
    }
}
