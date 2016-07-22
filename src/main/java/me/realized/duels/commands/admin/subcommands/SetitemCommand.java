package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.event.KitItemChangeEvent;
import me.realized.duels.kits.Kit;
import me.realized.duels.kits.KitManager;
import me.realized.duels.utilities.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetitemCommand extends SubCommand {

    private final KitManager manager;

    public SetitemCommand() {
        super("setitem", "setitem [name]", "Replaces the displayed item to held item for selected kit.", 2);
        this.manager = getInstance().getKitManager();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        ItemStack held;

        if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.7")) {
            held = player.getInventory().getItemInHand();
        } else {
            held = player.getInventory().getItemInMainHand();
        }

        if (held == null || held.getType() == Material.AIR) {
            pm(player, "&cPlease hold an item to use this command.");
            return;
        }

        String name = args[1];

        if (manager.getKit(name) == null) {
            pm(player, "&cA kit with that name was not found.");
            return;
        }

        Kit kit = manager.getKit(name);
        ItemStack old = kit.getDisplayed();
        ItemStack _new = held.clone();
        kit.setDisplayed(_new);
        manager.getGUI().update(manager.getKits());
        pm(player, Lang.REPLACE_KIT_ITEM.getMessage().replace("{NAME}", name));

        KitItemChangeEvent event = new KitItemChangeEvent(name, player, old, _new);
        Bukkit.getPluginManager().callEvent(event);
    }
}
