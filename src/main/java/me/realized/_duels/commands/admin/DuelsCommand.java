package me.realized._duels.commands.admin;

import me.realized._duels.commands.BaseCommand;
import me.realized._duels.commands.SubCommand;
import me.realized._duels.commands.admin.subcommands.*;
import me.realized._duels.utilities.Helper;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DuelsCommand extends BaseCommand {

    private final Map<String, SubCommand> children = new HashMap<>();

    private static final Set<String> DEFAULT_CHILDREN = new HashSet<>();

    public DuelsCommand() {
        super("duels", "duels.admin");

        children.put("create", new CreateCommand());
        children.put("delete", new DeleteCommand());
        children.put("edit", new EditCommand());
        children.put("info", new InfoCommand());
        children.put("list", new ListCommand());
        children.put("loadkit", new LoadkitCommand());
        children.put("savekit", new SavekitCommand());
        children.put("deletekit", new DeletekitCommand());
        children.put("reset", new ResetCommand());
        children.put("set", new SetCommand());
        children.put("setitem", new SetitemCommand());
        children.put("setlobby", new SetlobbyCommand());
        children.put("toggle", new ToggleCommand());
        children.put("playsound", new PlaysoundCommand());
        children.put("reload", new ReloadCommand());
        DEFAULT_CHILDREN.addAll(children.keySet());
    }

    @Override
    public void execute(Player sender, String[] args) {
        if (args.length == 0) {
            Helper.pm(sender, "Commands.duels.usage", true);
            return;
        }

        SubCommand child = children.get(args[0].toLowerCase());

        if (child == null) {
            Helper.pm(sender, "Errors.invalid-sub-command", true, "{ARGUMENT}", args[0]);
            return;
        }

        if (!sender.hasPermission(child.getPermission())) {
            Helper.pm(sender, "Errors.no-permission", true);
            return;
        }

        if (args.length < child.length()) {
            Helper.pm(sender, "Commands.duels.sub-command-usage", true, "{USAGE}", "/" + getName() + " " + child.getUsage(), "{DESCRIPTION}", child.getDescription());
            return;
        }

        child.execute(sender, args);
    }

    public boolean registerChildren(SubCommand command) {
        if (children.get(command.getName()) != null) {
            return false;
        }

        children.put(command.getName(), command);
        return true;
    }

    public boolean unregisterChildren(String name) {
        return !DEFAULT_CHILDREN.contains(name) && children.remove(name) != null;

    }
}
