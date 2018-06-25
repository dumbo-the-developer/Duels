package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.Kit;
import me.realized.duels.util.inventory.InventoryUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetitemCommand extends BaseCommand {

    public SetitemCommand(final DuelsPlugin plugin) {
        super(plugin, "setitem", "setitem [name]", "Sets the displayed item for selected kit.", 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final ItemStack held = InventoryUtil.getItemInHand(player);

        if (held == null || held.getType() == Material.AIR) {
            lang.sendMessage(sender, "ERROR.kit.empty-hand");
            return;
        }

        final String name = StringUtils.join(args, " ", 1, args.length);
        final Kit kit = kitManager.get(name);

        if (kit == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
            return;
        }

        kit.setDisplayed(held.clone());
        kitManager.getGui().calculatePages();
        lang.sendMessage(sender, "COMMAND.duels.setitem", "name", name);
    }
}
