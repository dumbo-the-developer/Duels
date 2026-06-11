package com.meteordevelopments.duels.command.commands.kit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.core.kit.KitImpl;
import com.meteordevelopments.duels.core.kit.edit.KitEditManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class KitresetCommand extends BaseCommand {

    public KitresetCommand(DuelsPlugin plugin) {
        super(plugin, "kitreset", "kit reset [player] <kitname>", "Resets an edited kit to its default.", 2, false);
    }

    public void executeCommand(CommandSender sender, String[] args) {
        if (args.length != 2 && args.length != 3) {
            lang.sendMessage(sender, "COMMAND.kit.reset.usage");
            return;
        }

        final Player target;
        final String kitName;

        if (args.length == 3) {
            if (!sender.hasPermission(Permissions.ADMIN) && !sender.hasPermission(Permissions.KIT_RESET_OTHERS)) {
                lang.sendMessage(sender, "ERROR.no-permission", "permission", Permissions.KIT_RESET_OTHERS);
                return;
            }

            target = Bukkit.getPlayerExact(args[1]);
            kitName = args[2].replace("-", " ");

            if (target == null || (sender instanceof Player player && !player.canSee(target))) {
                lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
                return;
            }
        } else {
            if (!(sender instanceof Player player)) {
                lang.sendMessage(sender, "COMMAND.kit.reset.usage");
                return;
            }

            target = player;
            kitName = args[1].replace("-", " ");
        }

        KitImpl kit = kitManager.get(kitName);
        if (kit == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", kitName);
            return;
        }

        KitEditManager editManager = KitEditManager.getInstance();
        if (editManager == null || !editManager.hasPlayerKit(target, kit.getName())) {
            lang.sendMessage(sender, "KIT.RESET.not-edited", "kit", kit.getName(), "name", target.getName());
            return;
        }

        if (!editManager.resetPlayerKit(target, kit.getName())) {
            lang.sendMessage(sender, "KIT.RESET.failed", "kit", kit.getName(), "name", target.getName());
            return;
        }

        if (target.equals(sender)) {
            lang.sendMessage(sender, "KIT.RESET.success", "kit", kit.getName());
        } else {
            lang.sendMessage(sender, "KIT.RESET.success-other", "kit", kit.getName(), "name", target.getName());
            lang.sendMessage(target, "KIT.RESET.reset-by-admin", "kit", kit.getName());
        }
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        executeCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) {
            if (sender.hasPermission(Permissions.ADMIN) || sender.hasPermission(Permissions.KIT_RESET_OTHERS)) {
                final String prefix = args[1].toLowerCase();
                List<String> completions = Bukkit.getOnlinePlayers().stream()
                        .filter(player -> !(sender instanceof Player viewer) || viewer.canSee(player))
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
                completions.addAll(handleTabCompletion(args[1], kitManager.getNames(false)));
                return completions;
            }

            return handleTabCompletion(args[1], kitManager.getNames(false));
        }

        if (args.length == 3 && (sender.hasPermission(Permissions.ADMIN) || sender.hasPermission(Permissions.KIT_RESET_OTHERS))) {
            return handleTabCompletion(args[2], kitManager.getNames(false));
        }

        return null;
    }
}
