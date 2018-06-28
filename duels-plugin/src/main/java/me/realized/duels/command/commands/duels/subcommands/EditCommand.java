package me.realized.duels.command.commands.duels.subcommands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.user.User;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.util.NumberUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class EditCommand extends BaseCommand {

    private final Map<String, BiConsumer<User, Integer>> actions = new HashMap<>();

    public EditCommand(final DuelsPlugin plugin) {
        super(plugin, "edit", "edit [name] [wins|losses] [amount]", "Sets player's wins or losses to given amount.", 4, false);
        actions.put("wins", User::setWins);
        actions.put("losses", User::setLosses);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final UserData user = userManager.get(args[1]);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "name", args[1]);
            return;
        }

        final String type = args[2].toLowerCase();
        final BiConsumer<User, Integer> action = actions.get(type);

        if (action == null) {
            lang.sendMessage(sender, "ERROR.command.invalid-option", "option", args[2]);
            return;
        }

        final int amount = Math.max(NumberUtil.parseInt(args[3]).orElse(0), 0);
        action.accept(user, amount);
        lang.sendMessage(sender, "COMMAND.duels.edit", "name", user.getName(), "type", type, "amount", amount);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 3) {
            return actions.keySet().stream()
                .filter(type -> type.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length > 3) {
            return Arrays.asList("10", "50", "100", "500", "1000");
        }

        return null;
    }
}
