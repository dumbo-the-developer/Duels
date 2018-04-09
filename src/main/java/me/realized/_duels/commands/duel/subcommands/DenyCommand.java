package me.realized._duels.commands.duel.subcommands;

import me.realized._duels.commands.SubCommand;
import me.realized._duels.data.UserData;
import me.realized._duels.dueling.Request;
import me.realized._duels.dueling.RequestManager;
import me.realized._duels.event.RequestHandleEvent;
import me.realized._duels.utilities.Helper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DenyCommand extends SubCommand {

    public DenyCommand() {
        super("deny", "deny [player]", "duels.duel", "Decline a duel request.", 2);
    }

    @Override
    public void execute(Player sender, String[] args) {
        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            Helper.pm(sender, "Errors.player-not-found", true);
            return;
        }

        UserData data = dataManager.getUser(target.getUniqueId(), false);

        if (data == null) {
            Helper.pm(sender, "Errors.player-not-found", true);
            return;
        }

        RequestManager.Result result = requestManager.hasRequestFrom(sender, target);

        if (result != RequestManager.Result.FOUND) {
            switch (result) {
                case TIMED_OUT:
                    Helper.pm(sender, "Errors.already-expired", true);
                    return;
                case NOT_FOUND:
                    Helper.pm(sender, "Errors.no-request", true);
                    return;
            }
        }

        Request request = requestManager.getRequestFrom(sender, target);
        requestManager.removeRequestFrom(sender, target);
        Helper.pm(sender, "Dueling.on-request-decline.sender", true, "{PLAYER}", target.getName());
        Helper.pm(target, "Dueling.on-request-decline.receiver", true, "{PLAYER}", sender.getName());

        RequestHandleEvent event = new RequestHandleEvent(request, sender, target, RequestHandleEvent.Action.DENIED);
        Bukkit.getPluginManager().callEvent(event);
    }
}
