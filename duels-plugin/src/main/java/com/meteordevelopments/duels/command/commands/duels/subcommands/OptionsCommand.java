package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.gui.options.OptionsGui;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OptionsCommand extends BaseCommand {

    public OptionsCommand(final DuelsPlugin plugin) {
        super(plugin, "options", "options [kit]", "Opens the options gui for kit.", 2, true, "useoption");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtil.join(args, " ", 1, args.length).replace("-", " ");
        final KitImpl kit = kitManager.get(name);

        if (kit == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
            return;
        }

        final Player player = (Player) sender;
        plugin.getGuiListener().addGui(player, new OptionsGui(plugin, player, kit), true).open(player);
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], kitManager.getNames(false));
        }

        return null;
    }
}
