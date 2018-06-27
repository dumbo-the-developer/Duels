package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.util.NumberUtil;
import org.bukkit.command.CommandSender;

public class EditCommand extends BaseCommand {

    public EditCommand(final DuelsPlugin plugin) {
        super(plugin, "edit", "edit [name] [wins|losses] [amount]", "Sets player's wins or losses to given amount.", 4, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final UserData user = userManager.get(args[1]);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "player", args[1]);
            return;
        }

        final int amount = Math.max(NumberUtil.parseInt(args[3]).orElse(0), 0);

        if (args[2].equalsIgnoreCase("wins")) {
            user.setWins(amount);
            lang.sendMessage(sender, "COMMAND.duels.edit", "name", user.getName(), "type", "wins", "amount", amount);
        } else {
            user.setLosses(amount);
            lang.sendMessage(sender, "COMMAND.duels.edit", "name", user.getName(), "type", "losses", "amount", amount);
        }
    }
}
