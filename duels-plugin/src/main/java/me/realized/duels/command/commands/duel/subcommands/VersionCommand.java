package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.extra.Permissions;
import me.realized.duels.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class VersionCommand extends BaseCommand {

    public VersionCommand(final DuelsPlugin plugin) {
        super(plugin, "version", Permissions.DUEL, "v");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final PluginDescriptionFile info = plugin.getDescription();
        sender.sendMessage(StringUtil.color("&9" + info.getFullName() + " by " + info.getAuthors().get(0)));
    }
}
