package me.realized.duels.commands.duel.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.configuration.Config;
import me.realized.duels.data.DataManager;
import me.realized.duels.data.UserData;
import me.realized.duels.dueling.Request;
import me.realized.duels.dueling.RequestManager;
import me.realized.duels.event.RequestHandleEvent;
import me.realized.duels.utilities.Helper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DenyCommand extends SubCommand {

    private final Config config;
    private final DataManager dataManager;
    private final RequestManager requestManager;

    public DenyCommand() {
        super("deny", "deny [player]", "Decline a duel request.", 2);
        this.config = getInstance().getConfiguration();
        this.dataManager = getInstance().getDataManager();
        this.requestManager = getInstance().getRequestManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            pm(sender, "&cPlayer not found.");
            return;
        }

        UserData data = dataManager.getUser(target.getUniqueId(), false);

        if (data == null) {
            pm(sender, "&cPlayer not found.");
            return;
        }

        RequestManager.Result result = requestManager.hasRequestFrom(player, target);

        if (result != RequestManager.Result.FOUND) {
            switch (result) {
                case TIMED_OUT:
                    pm(sender, "&cThat duel request has already expired.");
                    return;
                case NOT_FOUND:
                    pm(sender, "&cThat player did not send you a duel request!");
                    return;
            }
        }

        Request request = requestManager.getRequestFrom(player, target);
        requestManager.removeRequestFrom(player, target);
        Helper.pm(Helper.replaceWithArgs(config.getString("on-request-deny-target"), "{PLAYER}", target.getName()), player);
        Helper.pm(Helper.replaceWithArgs(config.getString("on-request-deny-sender"), "{PLAYER}", player.getName()), target);

        RequestHandleEvent event = new RequestHandleEvent(request, player, target, RequestHandleEvent.Action.DENIED);
        Bukkit.getPluginManager().callEvent(event);
    }
}
