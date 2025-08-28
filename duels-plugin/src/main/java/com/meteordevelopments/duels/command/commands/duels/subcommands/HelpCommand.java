package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.google.common.collect.Lists;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends BaseCommand {

    private final List<String> categories = Lists.newArrayList("arena", "kit", "queue", "sign", "user", "extra");

    public HelpCommand(final DuelsPlugin plugin) {
        super(plugin, "help", null, null, 1, false, "h");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length == getLength() || !categories.contains(args[1].toLowerCase())) {
            lang.sendMessage(sender, "COMMAND.duels.usage", "command", label);
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.help." + args[1].toLowerCase(), "command", label);
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return categories.stream()
                    .filter(category -> category.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return null;
    }
}
