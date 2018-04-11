package me.realized.duels.command.commands.duels.subcommands;

import java.util.Optional;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.Kit;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeletekitCommand extends BaseCommand {

    public DeletekitCommand(final DuelsPlugin plugin) {
        super(plugin, "deletekit", "deletekit [name]", null, 2, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtils.join(args, " ", 1, args.length);
        final Optional<Kit> target = kitManager.get(name);

        if (!target.isPresent()) {
            sender.sendMessage(name + " is not an existing kit.");
            return;
        }

        target.get().equip((Player) sender);
        sender.sendMessage("Equipped " + name + "!");
    }
}
