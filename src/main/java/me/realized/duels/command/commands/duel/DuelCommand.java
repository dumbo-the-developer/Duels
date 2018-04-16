package me.realized.duels.command.commands.duel;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duel.subcommands.StatsCommand;
import me.realized.duels.gui.setting.ArenaSelectButton;
import me.realized.duels.gui.setting.ItemBettingButton;
import me.realized.duels.gui.setting.KitSelectButton;
import me.realized.duels.gui.setting.RequestDetailsButton;
import me.realized.duels.util.gui.SinglePageGUI;
import me.realized.duels.util.inventory.InventoryUtil;
import me.realized.duels.util.inventory.ItemBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

        SinglePageGUI gui = setting.getGui();

        if (gui == null) {
            gui = new SinglePageGUI("Request Settings", 3);
            guiListener.addGUI(player, gui);
            final ItemStack spacing = ItemBuilder.of(Material.STAINED_GLASS_PANE).name(" ").build();
            InventoryUtil.fillRange(gui.getInventory(), 2, 7, spacing);
            InventoryUtil.fillRange(gui.getInventory(), 11, 16, spacing);
            gui.set(4, new RequestDetailsButton(plugin));
            gui.set(12, new KitSelectButton(plugin));
            gui.set(13, new ArenaSelectButton(plugin));
            gui.set(14, new ItemBettingButton(plugin));
            InventoryUtil.fillRange(gui.getInventory(), 20, 25, spacing);
            final ItemStack accept = ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 5).name("&a&lSEND REQUEST TO " + args[0]).build();
            InventoryUtil.fillSpace(gui.getInventory(), 0, 2, 3, accept);
            final ItemStack cancel = ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 14).name("&c&lCANCEL").build();
            InventoryUtil.fillSpace(gui.getInventory(), 7, 9, 3, cancel);
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
