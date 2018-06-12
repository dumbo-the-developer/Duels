package me.realized._duels.commands.other;

import me.realized._duels.commands.BaseCommand;
import me.realized._duels.utilities.Helper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SpectateCommand extends BaseCommand {

    public SpectateCommand() {
        super("spectate", "duels.spectate");
    }

    @Override
    public void execute(Player sender, String[] args) {
        if (spectatorManager.isSpectating(sender)) {
            spectatorManager.stopSpectating(sender);
            return;
        }

        if (args.length == 0) {
            Helper.pm(sender, "Commands.spectate.usage", true);
            return;
        }

        if (config.isSpectatingRequiresClearedInventory() && !Helper.hasEmptyInventory(sender)) {
            Helper.pm(sender, "Errors.empty-inventory-only", true);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            Helper.pm(sender, "Errors.player-not-found", true);
            return;
        }

        spectatorManager.spectate(sender, target);
    }
}
