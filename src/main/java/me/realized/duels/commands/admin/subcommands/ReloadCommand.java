package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.Core;
import me.realized.duels.commands.SubCommand;
import me.realized.duels.utilities.Helper;
import me.realized.duels.utilities.ReloadType;
import org.bukkit.entity.Player;

public class ReloadCommand extends SubCommand {

    // TODO: 10/19/16 Add validation, PreReloadAffect that shows what reloading now will do 

    public ReloadCommand() {
        super("reload", "reload [weak]", "duels.admin", "Reloads the plugin completely or only the messages. Append 'weak' to only reload messages file.", 1);
    }

    @Override
    public void execute(Player sender, String[] args) {
        Core instance = Core.getInstance();

        if (args.length > 1 && args[1].equalsIgnoreCase("weak")) {
            instance.reload(ReloadType.WEAK);
            Helper.pm(sender, "&a" + instance.getDescription().getFullName() + ": Weak reload complete.", false);
            return;
        }

        instance.reload(ReloadType.STRONG);
        Helper.pm(sender, "&a" + instance.getDescription().getFullName() + ": Reload complete.", false);
    }
}
