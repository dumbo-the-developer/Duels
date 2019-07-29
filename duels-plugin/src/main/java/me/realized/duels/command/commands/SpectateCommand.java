package me.realized.duels.command.commands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCommand extends BaseCommand {

    public SpectateCommand(final DuelsPlugin plugin) {
        super(plugin, "spectate", Permissions.SPECTATE, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (spectateManager.isSpectating(player)) {
            spectateManager.stopSpectating(player, false);
            return;
        }

        if (args.length == 0) {
            lang.sendMessage(sender, "COMMAND.spectate.usage", "command", label);
            return;
        }

        if (config.isSpecRequiresClearedInventory() && InventoryUtil.hasItem(player)) {
            lang.sendMessage(sender, "ERROR.duel.inventory-not-empty");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[0]);
            return;
        }

        spectateManager.startSpectating(player, target);
    }
}
