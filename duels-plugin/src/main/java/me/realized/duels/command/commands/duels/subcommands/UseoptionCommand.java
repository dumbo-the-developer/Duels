package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.Kit;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public class UseoptionCommand extends BaseCommand {

    public UseoptionCommand(final DuelsPlugin plugin) {
        super(plugin, "useoption", "useoption [kit] [usepermission:arenaspecific]", "Enable or disable options for kit.", 3, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtils.join(args, " ", 1, args.length - 1);
        final Kit kit = kitManager.get(name);

        if (kit == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
            return;
        }

        final String option = args[args.length - 1];

        if (option.equalsIgnoreCase("usepermission")) {
            kit.setUsePermission(!kit.isUsePermission());
            lang.sendMessage(sender, "COMMAND.duels.useoption", "option", option, "kit", kit.getName(), "value", kit.isUsePermission());
        } else if (option.equalsIgnoreCase("arenaspecific")) {
            kit.setArenaSpecific(!kit.isArenaSpecific());
            lang.sendMessage(sender, "COMMAND.duels.useoption", "option", option, "kit", kit.getName(), "value", kit.isArenaSpecific());
        } else {
            lang.sendMessage(sender, "ERROR.command.invalid-option", "option", option);
        }
    }
}
