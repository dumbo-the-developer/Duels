package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.core.kit.KitImpl;
import com.meteordevelopments.duels.core.kit.edit.KitEditManager;
import com.meteordevelopments.duels.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class KitgiveCommand extends BaseCommand {

    public KitgiveCommand(final DuelsPlugin plugin) {
        super(plugin, "kitgive", "kitgive [kit] [player]", "Gives a kit to a player.", 3, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String targetName = args[args.length - 1];
        final String kitName = StringUtil.join(args, " ", 1, args.length - 1).replace("-", " ");
        final KitImpl kit = kitManager.get(kitName);

        if (kit == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", kitName);
            return;
        }

        final Player target = Bukkit.getPlayerExact(targetName);

        if (target == null || (sender instanceof Player player && !player.canSee(target))) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", targetName);
            return;
        }

        target.getInventory().clear();

        boolean loaded = false;
        final KitEditManager editManager = KitEditManager.getInstance();

        if (editManager != null && editManager.hasPlayerKit(target, kit.getName())) {
            loaded = editManager.loadPlayerKit(target, kit.getName());
        }

        if (!loaded) {
            kit.equip(target);
        }

        lang.sendMessage(sender, "COMMAND.duels.kit-give", "kit", kit.getName(), "name", target.getName());
        lang.sendMessage(target, "COMMAND.duels.kit-give-target", "kit", kit.getName());
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], kitManager.getNames(false));
        }

        if (args.length == 3) {
            final String prefix = args[2].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }

        return null;
    }
}
