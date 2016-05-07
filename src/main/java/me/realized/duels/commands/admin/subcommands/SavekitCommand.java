package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.event.KitCreateEvent;
import me.realized.duels.kits.Kit;
import me.realized.duels.kits.KitManager;
import me.realized.duels.utilities.Lang;
import me.realized.duels.utilities.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SavekitCommand extends SubCommand {

    private final KitManager manager;

    public SavekitCommand() {
        super("savekit", "savekit [name]", "Save a new kit with your inventory contents.", 2);
        this.manager = getInstance().getKitManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String name = args[1];

        if (!StringUtil.isAlphanumeric(name)) {
            pm(sender, "&cKit name must be alphanumeric.");
            return;
        }

        if (manager.getKit(name) != null) {
            pm(player, "&cA kit with that name already exists.");
            return;
        }

        Kit kit = new Kit(name, player.getInventory());
        manager.addKit(name, kit);
        KitCreateEvent event = new KitCreateEvent(name, player);
        Bukkit.getPluginManager().callEvent(event);
        pm(player, Lang.KIT_CREATE.getMessage().replace("{NAME}", name));
    }
}
