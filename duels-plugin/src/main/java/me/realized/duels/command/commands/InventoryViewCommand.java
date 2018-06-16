package me.realized.duels.command.commands;

import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.extra.Permissions;
import me.realized.duels.gui.inventory.InventoryGui;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.profile.ProfileUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InventoryViewCommand extends BaseCommand {

    public InventoryViewCommand(final DuelsPlugin plugin) {
        super(plugin, "inventoryview", Permissions.DUEL, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(StringUtil.color("&cUsage: /inventoryview [uuid]"));
            return;
        }

        if (!ProfileUtil.isUUID(args[0])) {
            sender.sendMessage(StringUtil.color("&cInvalid UUID!"));
            return;
        }

        final InventoryGui gui = inventoryManager.get(UUID.fromString(args[0]));

        if (gui == null) {
            sender.sendMessage(StringUtil.color("&cNo inventory was found."));
            return;
        }

        gui.open((Player) sender);
    }
}
