package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.Kit;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoadkitCommand extends BaseCommand {

    public LoadkitCommand(final DuelsPlugin plugin) {
        super(plugin, "loadkit", "loadkit [name]", "Loads the selected kit to your inventory.", 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtils.join(args, " ", 1, args.length);
        final Kit kit = kitManager.get(name);

        if (kit == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
            return;
        }

        kit.equip((Player) sender);
        lang.sendMessage(sender, "COMMAND.duels.loadkit", "name", name);
    }
}
