package com.meteordevelopments.duels.replay.gui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.replay.data.DuelReplayMetadata;

public final class BedrockReplayForm {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private BedrockReplayForm() {
    }

    public static void send(final DuelsPlugin plugin, final Player player, final UUID targetPlayer) {
        final List<DuelReplayMetadata> list = targetPlayer != null 
                ? plugin.getReplayManager().getMetadataForPlayer(targetPlayer)
                : plugin.getReplayManager().getAllMetadata();

        final String formTitle;
        if (targetPlayer != null) {
            final OfflinePlayer offline = Bukkit.getOfflinePlayer(targetPlayer);
            final String name = offline.getName() != null ? offline.getName() : "Unknown";
            final String t = plugin.getLang().getMessage("GUI.replay-browser.player-title", "player", name);
            formTitle = t != null ? t : "Replays: " + name;
        } else {
            final String t = plugin.getLang().getMessage("GUI.replay-browser.title");
            formTitle = t != null ? t : "Duel Replays";
        }

        final SimpleForm.Builder builder = SimpleForm.builder().title(formTitle);

        if (list.isEmpty()) {
            final String emptyMsg = plugin.getLang().getMessage("GUI.replay-browser.buttons.empty.name");
            builder.content(emptyMsg != null ? emptyMsg : "No duel replays found.")
                    .button("Close");
            FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
            return;
        }

        builder.content("Select a replay to view options or watch:");

        for (final DuelReplayMetadata meta : list) {
            final String p1 = meta.getPlayer1Name() != null ? meta.getPlayer1Name() : "Unknown";
            final String p2 = meta.getPlayer2Name() != null ? meta.getPlayer2Name() : "Unknown";
            final String winner = meta.getWinnerName() != null ? meta.getWinnerName() : "None";
            final String duration = meta.getFormattedDuration();

            builder.button(p1 + " vs " + p2 + "\n§7Winner: §a" + winner + " §8| §e" + duration);
        }

        builder.validResultHandler(response -> {
            final int buttonId = response.clickedButtonId();
            if (buttonId >= 0 && buttonId < list.size()) {
                final DuelReplayMetadata selected = list.get(buttonId);
                sendDetails(plugin, player, selected, targetPlayer);
            }
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }

    public static void sendDetails(final DuelsPlugin plugin, final Player player, final DuelReplayMetadata metadata, final UUID targetPlayer) {
        final StringBuilder content = new StringBuilder();
        content.append("§e§lMatch Details\n\n");
        content.append("§7Replay ID: §f").append(metadata.getReplayId()).append("\n");
        content.append("§7Player 1: §b").append(metadata.getPlayer1Name() != null ? metadata.getPlayer1Name() : "Unknown").append("\n");
        content.append("§7Player 2: §b").append(metadata.getPlayer2Name() != null ? metadata.getPlayer2Name() : "Unknown").append("\n");
        content.append("§7Winner: §a").append(metadata.getWinnerName() != null ? metadata.getWinnerName() : "None").append("\n");
        content.append("§7Loser: §c").append(metadata.getLoserName() != null ? metadata.getLoserName() : "None").append("\n");
        content.append("§7Kit: §f").append(metadata.getKitName() != null ? metadata.getKitName() : "Own Inventory").append("\n");
        content.append("§7Arena: §f").append(metadata.getArenaName() != null ? metadata.getArenaName() : "Random").append("\n");
        content.append("§7Duration: §e").append(metadata.getFormattedDuration()).append("\n");
        if (metadata.getBetAmount() > 0) {
            content.append("§7Bet: §6$").append(metadata.getBetAmount()).append("\n");
        }
        content.append("§7End Reason: §7").append(metadata.getEndReason() != null ? metadata.getEndReason() : "OTHER").append("\n");
        content.append("§7Date: §8").append(DATE_FORMAT.format(new Date(metadata.getStartTime()))).append("\n");

        final String titleMsg = plugin.getLang().getMessage("GUI.replay-details.title", "id", metadata.getReplayId());
        final SimpleForm.Builder builder = SimpleForm.builder()
                .title(titleMsg != null ? titleMsg : "Replay: " + metadata.getReplayId())
                .content(content.toString())
                .button("§a▶ Watch Replay");

        final boolean isAdmin = player.hasPermission(Permissions.ADMIN) || player.hasPermission(Permissions.REPLAY_ADMIN);
        if (isAdmin) {
            builder.button("§c✖ Delete Replay");
        }
        builder.button("§7« Back");

        builder.validResultHandler(response -> {
            final int buttonId = response.clickedButtonId();
            if (buttonId == 0) {
                plugin.getReplayManager().playReplay(player, metadata.getReplayId());
            } else if (isAdmin && buttonId == 1) {
                plugin.getReplayManager().deleteReplay(metadata.getReplayId());
                plugin.getLang().sendMessage(player, "REPLAY.deleted", "id", metadata.getReplayId());
                send(plugin, player, targetPlayer);
            } else {
                send(plugin, player, targetPlayer);
            }
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
