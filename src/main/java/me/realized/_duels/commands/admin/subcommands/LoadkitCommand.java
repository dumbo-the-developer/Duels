package me.realized._duels.commands.admin.subcommands;

import me.realized._duels.commands.SubCommand;
import me.realized._duels.event.KitLoadEvent;
import me.realized._duels.kits.Kit;
import me.realized._duels.utilities.Helper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LoadkitCommand extends SubCommand {

    public LoadkitCommand() {
        super("loadkit", "loadkit [name]", "duels.admin", "Load a kit with the given name.", 2);
    }

    @Override
    public void execute(Player sender, String[] args) {
        String name = Helper.join(args, 1, args.length, " ");

        if (kitManager.getKit(name) == null) {
            Helper.pm(sender, "Errors.kit-not-found", true);
            return;
        }

        Kit kit = kitManager.getKit(name);
        kit.equip(sender);
        Helper.pm(sender, "Kits.loaded", true, "{NAME}", name);
        KitLoadEvent event = new KitLoadEvent(name, kit, sender);
        Bukkit.getPluginManager().callEvent(event);
    }
}
