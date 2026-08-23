package com.meteordevelopments.duels.command.commands;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.config.CommandsConfig.CommandSettings;
import com.meteordevelopments.duels.replay.data.DuelReplayMetadata;
import com.meteordevelopments.duels.replay.gui.BedrockReplayForm;
import com.meteordevelopments.duels.replay.gui.ReplayBrowserGui;
import com.meteordevelopments.duels.replay.playback.Replayer;
import com.meteordevelopments.duels.util.FloodgateUtil;

public class ReplayCommand extends BaseCommand {

    public ReplayCommand(final DuelsPlugin plugin, final CommandSettings settings) {
        super(plugin, Objects.requireNonNull(settings, "settings").getName(), Permissions.REPLAY, true, settings.getAliasArray());
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (!(sender instanceof Player player)) {
            lang.sendMessage(sender, "COMMAND.replay.only-players");
            return;
        }

        if (args.length == 0) {
            openReplays(player, null);
            return;
        }

        final String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("stop") || sub.equals("leave") || sub.equals("quit")) {
            if (plugin.getReplayManager().isWatching(player)) {
                final Replayer replayer = plugin.getReplayManager().getReplayer(player);
                if (replayer != null) {
                    replayer.stop();
                }
            } else {
                lang.sendMessage(player, "REPLAY.not-watching");
            }
            return;
        }

        if (sub.equals("play") || sub.equals("watch")) {
            if (args.length < 2) {
                lang.sendMessage(player, "COMMAND.replay.usage", "command", label);
                return;
            }
            plugin.getReplayManager().playReplay(player, args[1]);
            return;
        }

        if (sub.equals("delete") || sub.equals("remove")) {
            if (!player.hasPermission(Permissions.REPLAY_ADMIN) && !player.hasPermission(Permissions.ADMIN)) {
                lang.sendMessage(player, "REPLAY.delete-no-permission");
                return;
            }
            if (args.length < 2) {
                lang.sendMessage(player, "COMMAND.replay.usage", "command", label);
                return;
            }
            plugin.getReplayManager().deleteReplay(args[1]);
            lang.sendMessage(player, "REPLAY.deleted", "id", args[1]);
            return;
        }

        if (sub.equals("list")) {
            if (args.length >= 2) {
                final OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                openReplays(player, target.getUniqueId());
            } else {
                openReplays(player, null);
            }
            return;
        }

        // Check if argument matches an existing replay ID
        final DuelReplayMetadata meta = plugin.getReplayManager().getMetadata(args[0]);
        if (meta != null) {
            plugin.getReplayManager().playReplay(player, args[0]);
            return;
        }

        // Check if argument is a player name to filter
        final OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target != null && (target.hasPlayedBefore() || target.isOnline())) {
            openReplays(player, target.getUniqueId());
            return;
        }

        lang.sendMessage(player, "COMMAND.replay.unknown", "name", args[0], "command", label);
    }

    private void openReplays(final Player player, final UUID targetPlayer) {
        if (FloodgateUtil.isBedrockPlayer(player)) {
            BedrockReplayForm.send(plugin, player, targetPlayer);
        } else {
            ReplayBrowserGui.open(plugin, player, targetPlayer);
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final org.bukkit.command.Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            final List<String> suggestions = new ArrayList<>();
            suggestions.add("list");
            suggestions.add("play");
            suggestions.add("stop");
            if (sender.hasPermission(Permissions.REPLAY_ADMIN) || sender.hasPermission(Permissions.ADMIN)) {
                suggestions.add("delete");
            }
            for (final DuelReplayMetadata meta : plugin.getReplayManager().getAllMetadata()) {
                suggestions.add(meta.getReplayId());
            }
            for (final Player online : Bukkit.getOnlinePlayers()) {
                suggestions.add(online.getName());
            }
            return handleTabCompletion(args[0], suggestions);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("play") || args[0].equalsIgnoreCase("delete"))) {
            final List<String> replayIds = plugin.getReplayManager().getAllMetadata().stream()
                    .map(DuelReplayMetadata::getReplayId)
                    .collect(Collectors.toList());
            return handleTabCompletion(args[1], replayIds);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
            final List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            return handleTabCompletion(args[1], playerNames);
        }

        return Collections.emptyList();
    }
}
