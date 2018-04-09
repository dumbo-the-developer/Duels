package me.realized._duels.commands.admin.subcommands;

import me.realized._duels.commands.SubCommand;
import me.realized._duels.data.UserData;
import me.realized._duels.utilities.Helper;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EditCommand extends SubCommand {

    public EditCommand() {
        super("edit", "edit [player] [add:remove:set] [wins:losses] [quantity]", "duels.admin", "Edit player's stats.", 5);
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

        if (!UserData.EditType.isValue(args[2].toUpperCase())) {
            Helper.pm(sender, "Errors.edit-failed", true, "{REASON}", args[2] + " is not a valid edit type. You may choose from one of the following: add, remove, set");
            return;
        }

        UserData.EditType editType = UserData.EditType.valueOf(args[2].toUpperCase());

        if (!UserData.StatsType.isValue(args[3].toUpperCase())) {
            Helper.pm(sender, "Errors.edit-failed", true, "{REASON}",  args[3] + " is not a valid stats type. You may choose from one of the following: wins, losses");
            return;
        }

        UserData.StatsType statsType = UserData.StatsType.valueOf(args[3].toUpperCase());

        if (!Helper.isInt(args[4], false)) {
            Helper.pm(sender, "Errors.edit-failed", true, "{REASON}", args[4] + " is not a valid amount.");
            return;
        }

        int amount = Integer.parseInt(args[4]);

        target.edit(editType, statsType, amount);
        String action = editType.name().toLowerCase() + " " + amount + " " + statsType.name().toLowerCase();
        Helper.pm(sender, "Stats.edit", true, "{PLAYER}", target.getName(), "{ACTION}", action);
    }
}
