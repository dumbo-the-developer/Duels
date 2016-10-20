package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.event.KitRemoveEvent;
import me.realized.duels.utilities.Helper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DeletekitCommand extends SubCommand {

    public DeletekitCommand() {
        super("deletekit", "deletekit [name]", "duels.admin", "Delete a kit with the given name.", 2);
    }

    @Override
    public void execute(Player sender, String[] args) {
        String name = Helper.join(args, 1, args.length, " ");

        if (kitManager.getKit(name) == null) {
            Helper.pm(sender, "Errors.kit-not-found", true);
            return;
        }

        kitManager.removeKit(name);
        KitRemoveEvent event = new KitRemoveEvent(name, sender);
        Bukkit.getPluginManager().callEvent(event);
        Helper.pm(sender, "Kits.deleted", true, "{NAME}", name);
    }
}
