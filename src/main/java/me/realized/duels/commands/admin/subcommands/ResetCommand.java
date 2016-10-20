package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.utilities.Helper;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ResetCommand extends SubCommand {

    public ResetCommand() {
        super("reset", "reset [player]", "duels.admin", "Completely reset a player's stats.", 2);
    }

    @Override
    public void execute(Player sender, String[] args) {
        UUID uuid = Helper.getUUID(args[1]);

        if (uuid == null) {
            Helper.pm(sender, "Errors.player-not-found", true);
            return;
        }

        UserData target = dataManager.getUser(uuid, true);

        if (target == null) {
            Helper.pm(sender, "Errors.player-not-found", true);
            return;
        }

        target.edit(UserData.EditType.SET, UserData.StatsType.WINS, 0);
        target.edit(UserData.EditType.SET, UserData.StatsType.LOSSES, 0);
        target.getMatches().clear();
        Helper.pm(sender, "Stats.reset", true, "{PLAYER}", target.getName());
    }
}
