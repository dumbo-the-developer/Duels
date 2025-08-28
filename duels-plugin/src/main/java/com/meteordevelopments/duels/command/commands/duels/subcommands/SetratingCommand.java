package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.data.UserData;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.util.NumberUtil;
import com.meteordevelopments.duels.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SetratingCommand extends BaseCommand {

    public SetratingCommand(final DuelsPlugin plugin) {
        super(plugin, "setrating", "setrating [name] [-:kit] [amount]", "Sets player's rating for kit.", 4, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final UserData user = userManager.get(args[1]);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "name", args[1]);
            return;
        }

        KitImpl kit = null;

        if (!args[2].equals("-")) {
            final String name = StringUtil.join(args, " ", 2, args.length - 1).replace("-", " ");
            kit = kitManager.get(name);

            if (kit == null) {
                lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
                return;
            }
        }

        final String kitName = kit != null ? kit.getName() : lang.getMessage("GENERAL.none");
        final int rating = Math.max(NumberUtil.parseInt(args[args.length - 1]).orElse(config.getDefaultRating()), 0);
        user.setRating(kit, rating);
        lang.sendMessage(sender, "COMMAND.duels.set-rating", "name", user.getName(), "kit", kitName, "rating", rating);
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 3) {
            return handleTabCompletion(args[2], kitManager.getNames(true));
        }

        if (args.length > 3) {
            return Arrays.asList("0", "10", "50", "100", "500", "1000");
        }

        return null;
    }
}
