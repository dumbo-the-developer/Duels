package me.realized.duels.commands.duel.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.dueling.Request;
import me.realized.duels.dueling.RequestManager;
import me.realized.duels.event.RequestHandleEvent;
import me.realized.duels.hooks.CombatTagPlusHook;
import me.realized.duels.utilities.Helper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class AcceptCommand extends SubCommand {

    private final CombatTagPlusHook ctHook;

    public AcceptCommand() {
        super("accept", "accept [player]", "duels.duel", "Accept a duel request.", 2);
        this.ctHook = (CombatTagPlusHook) hookManager.get("CombatTagPlus");
    }

    @Override
    public void execute(Player sender, String[] args) {
        if (sender.getGameMode() == GameMode.CREATIVE && config.isPatchesDisallowCreativeDueling()) {
            Helper.pm(sender, "Errors.is-in-creative", true);
            return;
        }

        if (ctHook.isTagged(sender)) {
            Helper.pm(sender, "Errors.is-combat-tagged", true);
            return;
        }

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

        if (arenaManager.isInMatch(sender)) {
            Helper.pm(sender, "Errors.already-in-match.sender", true);
            return;
        }

        if (arenaManager.isInMatch(target)) {
            Helper.pm(sender, "Errors.already-in-match.target", true);
            return;
        }

        if(target.isDead()) {
            Helper.pm(sender, "Errors.alive-only", true);
            return;
        }

        Request request = requestManager.getRequestFrom(sender, target);
        requestManager.removeRequestFrom(sender, target);
        duelManager.startMatch(sender, target, request);

        RequestHandleEvent event = new RequestHandleEvent(request, sender, target, RequestHandleEvent.Action.ACCEPTED);
        Bukkit.getPluginManager().callEvent(event);
    }
}
