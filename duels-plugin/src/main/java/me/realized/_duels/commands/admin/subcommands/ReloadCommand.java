package me.realized._duels.commands.admin.subcommands;

import me.realized._duels.Core;
import me.realized._duels.commands.SubCommand;
import me.realized._duels.utilities.Helper;
import me.realized._duels.utilities.Reloadable;
import org.bukkit.entity.Player;

public class ReloadCommand extends SubCommand {

    public ReloadCommand() {
        super("reload", "reload [weak]", "duels.admin", "Reloads the plugin completely or only the messages. Append 'weak' to only reload messages file.", 1);
    }

    @Override
    public void execute(Player sender, String[] args) {
        Core instance = Core.getInstance();

        if (args.length > 1 && args[1].equalsIgnoreCase("weak")) {
            instance.reload(Reloadable.ReloadType.WEAK);
            Helper.pm(sender, "&a" + instance.getDescription().getFullName() + ": Weak reload complete.", false);
            return;
        }

        instance.reload(Reloadable.ReloadType.STRONG);
        Helper.pm(sender, "&a" + instance.getDescription().getFullName() + ": Reload complete.", false);
    }
}
