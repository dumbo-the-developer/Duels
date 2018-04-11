package me.realized.duels.command.commands.duels;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duels.subcommands.DeletekitCommand;
import me.realized.duels.command.commands.duels.subcommands.LoadkitCommand;
import me.realized.duels.command.commands.duels.subcommands.SavekitCommand;
import me.realized.duels.data.ItemData;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DuelsCommand extends BaseCommand {

    public DuelsCommand(final DuelsPlugin plugin) {
        super(plugin, "duels", "duels.admin", true);
        child(new LoadkitCommand(plugin), new SavekitCommand(plugin), new DeletekitCommand(plugin));
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final ItemStack item = ((Player) sender).getItemInHand();

        if (item != null && item.getType() != Material.AIR) {
            System.out.println(plugin.getGson().toJson(new ItemData(item)));
        } else if (args.length > 0) {
            final ItemData data = plugin.getGson().fromJson(StringUtils.join(args, " "), ItemData.class);
            ((Player) sender).getInventory().addItem(data.toItemStack());
        }
    }
}
