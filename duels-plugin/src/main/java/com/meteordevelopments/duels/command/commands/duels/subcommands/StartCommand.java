package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.google.common.collect.Lists;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.core.arena.ArenaImpl;
import com.meteordevelopments.duels.core.kit.KitImpl;
import com.meteordevelopments.duels.setting.Settings;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class StartCommand extends BaseCommand {

    public StartCommand(final DuelsPlugin plugin) {
        super(plugin, "start", "start <player1> <player2> [kit] [arena]", "Forcefully starts a duel between two players.", Permissions.ADMIN, 3, false, "forcestart", "begin");
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length < 3) {
            lang.sendMessage(sender, "COMMAND.duels.start.usage", "command", label);
            return;
        }

        final Player player1 = Bukkit.getPlayerExact(args[1]);

        if (player1 == null || (sender instanceof Player p && !p.canSee(player1))) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
            return;
        }

        final Player player2 = Bukkit.getPlayerExact(args[2]);

        if (player2 == null || (sender instanceof Player p && !p.canSee(player2))) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[2]);
            return;
        }

        if (player1.equals(player2)) {
            lang.sendMessage(sender, "COMMAND.duels.start.same-player");
            return;
        }

        final Settings settings = new Settings(plugin);

        // Parse optional kit: args[3]
        if (args.length > 3) {
            final String kitArg = args[3].toLowerCase();
            if (kitArg.equals("-") || kitArg.equals("none") || kitArg.equals("own") || kitArg.equals("own_inventory") || kitArg.equals("owninventory")) {
                settings.setOwnInventory(true);
            } else {
                final String kitName = args[3].replace("-", " ");
                final KitImpl kit = kitManager.get(kitName);

                if (kit == null) {
                    lang.sendMessage(sender, "ERROR.kit.not-found", "name", kitName);
                    return;
                }

                settings.setKit(kit);
            }
        } else {
            // Default to own inventory if kit argument omitted
            settings.setOwnInventory(true);
        }

        // Parse optional arena: args[4]
        if (args.length > 4) {
            final String arenaArg = args[4].toLowerCase();
            if (!arenaArg.equals("-") && !arenaArg.equals("random")) {
                final String arenaName = args[4].replace("-", " ");
                final ArenaImpl arena = arenaManager.get(arenaName);

                if (arena == null) {
                    lang.sendMessage(sender, "ERROR.arena.not-found", "name", arenaName);
                    return;
                }

                settings.setArena(arena);
            }
        }

        duelManager.forceStartMatch(sender, player1, player2, settings);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            final String prefix = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !(sender instanceof Player sp) || sp.canSee(p))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .sorted(String::compareToIgnoreCase)
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            final String prefix = args[2].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.getName().equalsIgnoreCase(args[1]))
                    .filter(p -> !(sender instanceof Player sp) || sp.canSee(p))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .sorted(String::compareToIgnoreCase)
                    .collect(Collectors.toList());
        }

        if (args.length == 4) {
            final List<String> kits = Lists.newArrayList("-");
            kits.addAll(kitManager.getNames(false));
            return handleTabCompletion(args[3], kits);
        }

        if (args.length == 5) {
            final List<String> arenas = Lists.newArrayList("-", "random");
            arenas.addAll(arenaManager.getNames());
            return handleTabCompletion(args[4], arenas);
        }

        return null;
    }
}
