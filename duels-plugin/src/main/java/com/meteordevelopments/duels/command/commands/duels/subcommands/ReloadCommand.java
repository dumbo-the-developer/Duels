package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Reloadable;
import com.meteordevelopments.duels.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ReloadCommand extends BaseCommand {

    public ReloadCommand(final DuelsPlugin plugin) {
        super(plugin, "reload", null, null, 1, false, "rl");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length > getLength()) {
            final Loadable target = plugin.find(args[1]);

            if (!(target instanceof Reloadable)) {
                sender.sendMessage(ChatColor.RED + "Invalid module. The following modules are available for a reload: " + StringUtil.join(plugin.getReloadables(), ", "));
                return;
            }

            final String name = target.getClass().getSimpleName();

            if (plugin.reload(target)) {
                sender.sendMessage(ChatColor.GREEN + "[" + plugin.getDescription().getFullName() + "] Successfully reloaded " + name + ".");
            } else {
                sender.sendMessage(ChatColor.RED + "An error occured while reloading " + name + "! Please check the console for more information.");
            }

            return;
        }

        if (plugin.reload()) {
            sender.sendMessage(ChatColor.GREEN + "[" + plugin.getDescription().getFullName() + "] Reload complete.");
        } else {
            sender.sendMessage(ChatColor.RED + "An error occured while reloading the plugin! Please check the console for more information.");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return plugin.getReloadables().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return null;
    }
}
