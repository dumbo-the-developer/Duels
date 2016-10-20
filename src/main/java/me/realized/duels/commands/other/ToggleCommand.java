package me.realized.duels.commands.other;

import me.realized.duels.commands.BaseCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.utilities.Helper;
import org.bukkit.entity.Player;

public class ToggleCommand extends BaseCommand {

    public ToggleCommand() {
        super("toggle", "duels.toggle");
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
