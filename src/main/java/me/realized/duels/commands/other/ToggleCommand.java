package me.realized.duels.commands.other;

import me.realized.duels.commands.BaseCommand;
import me.realized.duels.configuration.Config;
import me.realized.duels.data.DataManager;
import me.realized.duels.data.UserData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ToggleCommand extends BaseCommand {

    private final Config config;
    private final DataManager manager;

    public ToggleCommand() {
        super("toggle", "duels.toggle");
        this.config = getInstance().getConfiguration();
        this.manager = getInstance().getDataManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UserData data = manager.getUser(player.getUniqueId(), true);

        if (data == null) {
            pm(sender, "&c&lYour data is improperly loaded. Please try re-logging.");
            return;
        }

        data.setRequestEnabled(!data.canRequest());
        pm(sender, (data.canRequest() ? config.getString("on-requests-enable") : config.getString("on-requests-disable")));
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
