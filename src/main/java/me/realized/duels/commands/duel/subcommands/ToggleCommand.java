package me.realized.duels.commands.duel.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.utilities.Helper;
import org.bukkit.entity.Player;

public class ToggleCommand extends SubCommand {

    public ToggleCommand() {
        super("toggle", "toggle", "duels.toggle", "Toggle your duel requests.", 1);
    }

    @Override
    public void execute(Player sender, String[] args) {
        UserData data = dataManager.getUser(sender.getUniqueId(), true);

        if (data == null) {
            Helper.pm(sender, "&c&lYour data is improperly loaded. Please try re-logging.", false);
            return;
        }

        data.setRequestEnabled(!data.canRequest());
        Helper.pm(sender, (data.canRequest() ? "Toggle.enabled" : "Toggle.disabled"), true);
    }
}
