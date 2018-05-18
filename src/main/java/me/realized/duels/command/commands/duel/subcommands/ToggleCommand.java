package me.realized.duels.command.commands.duel.subcommands;

import java.util.Optional;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.data.UserData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleCommand extends BaseCommand {

    public ToggleCommand(final DuelsPlugin plugin) {
        super(plugin, "toggle", "toggle", "Toggle your duel requests.", "duels.toggle", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Optional<UserData> cached = userManager.get((Player) sender);

        if (!cached.isPresent()) {
            lang.sendMessage(sender, "ERROR.data-load-failure");
            return;
        }

        final UserData data = cached.get();
        data.setRequests(!data.canRequest());
        lang.sendMessage(sender, "COMMAND.duel.toggle." + (data.canRequest() ? "enabled" : "disabled"));
    }
}
