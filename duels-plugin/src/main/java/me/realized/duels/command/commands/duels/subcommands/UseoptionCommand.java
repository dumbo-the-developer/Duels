package me.realized.duels.command.commands.duels.subcommands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.Kit;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class UseoptionCommand extends BaseCommand {

    private final Map<String, Consumer<Kit>> setters = new HashMap<>();
    private final Map<String, Function<Kit, Boolean>> getters = new HashMap<>();

    public UseoptionCommand(final DuelsPlugin plugin) {
        super(plugin, "useoption", "useoption [kit] [usepermission:arenaspecific]", "Enable or disable options for kit.", 3, false);
        setters.put("usepermission", kit -> kit.setUsePermission(!kit.isUsePermission()));
        setters.put("arenaspecific", kit -> kit.setArenaSpecific(!kit.isArenaSpecific()));
        getters.put("usepermission", Kit::isUsePermission);
        getters.put("arenaspecific", Kit::isArenaSpecific);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtils.join(args, " ", 1, args.length - 1).replace("-", " ");
        final Kit kit = kitManager.get(name);

        if (kit == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
            return;
        }

        final String option = args[args.length - 1];
        final Consumer<Kit> action = setters.get(option);

        if (action == null) {
            lang.sendMessage(sender, "ERROR.command.invalid-option", "option", option);
            return;
        }

        action.accept(kit);
        lang.sendMessage(sender, "COMMAND.duels.use-option", "option", option, "kit", kit.getName(), "value", getters.get(option).apply(kit));
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], kitManager.getNames());
        }

        if (args.length > 2) {
            return setters.keySet().stream()
                .filter(type -> type.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
        }

        return null;
    }
}
