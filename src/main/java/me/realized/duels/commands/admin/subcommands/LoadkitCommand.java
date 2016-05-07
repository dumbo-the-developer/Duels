package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.event.KitLoadEvent;
import me.realized.duels.kits.Kit;
import me.realized.duels.kits.KitManager;
import me.realized.duels.utilities.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoadkitCommand extends SubCommand {

    private final KitManager manager;

    public LoadkitCommand() {
        super("loadkit", "loadkit [name]", "Load a kit with the given name.", 2);
        this.manager = getInstance().getKitManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String name = args[1];

        if (manager.getKit(name) == null) {
            pm(player, "&cA kit with that name was not found.");
            return;
        }

        Kit kit = manager.getKit(name);
        kit.equip(player);
        pm(player, Lang.KIT_LOAD.getMessage().replace("{NAME}", name));
        KitLoadEvent event = new KitLoadEvent(name, kit, player);
        Bukkit.getPluginManager().callEvent(event);
    }
}
