package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.event.KitCreateEvent;
import me.realized.duels.kits.Kit;
import me.realized.duels.utilities.Helper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SavekitCommand extends SubCommand {

    public SavekitCommand() {
        super("savekit", "savekit [name]", "duels.admin", "Save a new kit with your inventory contents.", 2);
    }

    @Override
    public void execute(Player sender, String[] args) {
        String name = Helper.join(args, 1, args.length, " ");

        if (!Helper.isAlphanumeric(name)) {
            Helper.pm(sender, "Errors.must-be-alphanumeric", true);
            return;
        }

        if (kitManager.getKit(name) != null) {
            Helper.pm(sender, "Errors.kit-exists", true);
            return;
        }

        Kit kit = new Kit(name, sender.getInventory());
        kitManager.addKit(name, kit);
        KitCreateEvent event = new KitCreateEvent(name, sender);
        Bukkit.getPluginManager().callEvent(event);
        Helper.pm(sender, "Kits.created", true, "{NAME}", name);
    }
}
