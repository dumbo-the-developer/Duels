package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.kit.Kit;
import me.realized.duels.util.NumberUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public class SetratingCommand extends BaseCommand {

    public SetratingCommand(final DuelsPlugin plugin) {
        super(plugin, "setrating", "setrating [name] [kit] [rating]", "Sets player's rating for kit.", 4, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final UserData user = userManager.get(args[1]);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "player", args[1]);
            return;
        }

        final String name = StringUtils.join(args, " ", 2, args.length - 1);
        final Kit kit = kitManager.get(name);

        if (kit == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
            return;
        }

        final int rating = Math.max(NumberUtil.parseInt(args[args.length - 1]).orElse(config.getDefaultRating()), 0);
        user.setRating(kit.getName(), rating);
        lang.sendMessage(sender, "COMMAND.duels.setrating", "name", user.getName(), "kit", kit.getName(), "rating", rating);
    }
}
