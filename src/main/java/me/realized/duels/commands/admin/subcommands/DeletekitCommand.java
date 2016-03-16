package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.event.KitRemoveEvent;
import me.realized.duels.kits.KitManager;
import me.realized.duels.utilities.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeletekitCommand extends SubCommand {

    private final KitManager manager;

    public DeletekitCommand() {
        super("deletekit", "deletekit [name]", "Delete a kit with the given name.", 2);
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

        manager.removeKit(name);
        KitRemoveEvent event = new KitRemoveEvent(name, player);
        Bukkit.getPluginManager().callEvent(event);
        pm(player, Lang.KIT_DELETE.getMessage().replace("{NAME}", name));
    }
}
