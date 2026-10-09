package com.meteordevelopments.duels.replay.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.replay.data.DuelReplayMetadata;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.Button;

public class ReplayEntryButton extends Button<DuelsPlugin> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final DuelReplayMetadata metadata;
    private final ReplayBrowserGui parentGui;

    public ReplayEntryButton(final DuelsPlugin plugin, final DuelReplayMetadata metadata, final ReplayBrowserGui parentGui) {
        super(plugin, createDisplayItem(metadata));
        this.metadata = metadata;
        this.parentGui = parentGui;
    }

    private static ItemStack createDisplayItem(final DuelReplayMetadata metadata) {
        ItemStack item = Items.HEAD.clone();
        if (metadata.getWinnerName() != null && !metadata.getWinnerName().isEmpty()) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta != null) {
                meta.setOwner(metadata.getWinnerName());
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    @Override
    public void update(final Player player) {
        final String p1 = metadata.getPlayer1Name() != null ? metadata.getPlayer1Name() : "Unknown";
        final String p2 = metadata.getPlayer2Name() != null ? metadata.getPlayer2Name() : "Unknown";
        final String winner = metadata.getWinnerName() != null ? metadata.getWinnerName() : "None";
        final String loser = metadata.getLoserName() != null ? metadata.getLoserName() : "None";
        final String kit = metadata.getKitName() != null ? metadata.getKitName() : "Own Inventory";
        final String arena = metadata.getArenaName() != null ? metadata.getArenaName() : "Random";
        final String duration = metadata.getFormattedDuration();
        final String rawReason = metadata.getEndReason() != null ? metadata.getEndReason() : "OTHER";
        final String reason = ReplayDetailsGui.resolveReason(plugin, rawReason);
        final String date = DATE_FORMAT.format(new Date(metadata.getStartTime()));

        // Display name from lang.yml: GUI.replay-browser.buttons.entry.name
        final String nameMsg = plugin.getLang().getMessage(
                "GUI.replay-browser.buttons.entry.name",
                "player1", p1,
                "player2", p2
        );
        setDisplayName(nameMsg != null ? nameMsg : "&eReplay: &b" + p1 + " &7vs &b" + p2);

        // Main lore from lang.yml: GUI.replay-browser.buttons.entry.lore
        // The list is joined with \n by Lang, so we split it back.
        final String loreLine = plugin.getLang().getMessage(
                "GUI.replay-browser.buttons.entry.lore",
                "id", metadata.getReplayId(),
                "winner", winner,
                "loser", loser,
                "kit", kit,
                "arena", arena,
                "duration", duration,
                "reason", reason,
                "date", date
        );

        final List<String> lore = new ArrayList<>();
        if (loreLine != null) {
            for (final String line : loreLine.split("\n")) {
                lore.add(line);
            }
        } else {
            // Fallback if lang key is missing
            lore.add("&7ID: &8" + metadata.getReplayId());
            lore.add("&7Winner: &a" + winner);
            lore.add("&7Loser: &c" + loser);
            lore.add("&7Kit: &f" + kit);
            lore.add("&7Arena: &f" + arena);
            lore.add("&7Duration: &e" + duration);
            lore.add("&7End Reason: &7" + reason);
            lore.add("&7Date: &8" + date);
            lore.add(" ");
            lore.add("&e► Left-Click to view options / watch");
        }

        if (metadata.getBetAmount() > 0) {
            final String betLine = plugin.getLang().getMessage(
                    "GUI.replay-browser.buttons.entry.lore-bet", "bet", metadata.getBetAmount());
            lore.add(betLine != null ? betLine : "&7Bet: &6$" + metadata.getBetAmount());
        }

        // Admin lore from lang.yml: GUI.replay-browser.buttons.entry.lore-admin
        if (player.hasPermission(Permissions.ADMIN) || player.hasPermission(Permissions.REPLAY_ADMIN)) {
            final String loreAdmin = plugin.getLang().getMessage("GUI.replay-browser.buttons.entry.lore-admin");
            if (loreAdmin != null) {
                for (final String line : loreAdmin.split("\n")) {
                    lore.add(line);
                }
            } else {
                lore.add("&c► Shift-Right-Click to delete");
            }
        }

        setLore(lore);
    }

    @Override
    public void onClick(final Player player, final InventoryClickEvent event) {
        if (event.getClick() == ClickType.SHIFT_RIGHT && (player.hasPermission(Permissions.ADMIN) || player.hasPermission(Permissions.REPLAY_ADMIN))) {
            plugin.getReplayManager().deleteReplay(metadata.getReplayId());
            plugin.getLang().sendMessage(player, "REPLAY.deleted", "id", metadata.getReplayId());
            if (parentGui != null) {
                parentGui.refresh(player);
            }
            return;
        }

        ReplayDetailsGui.open(plugin, player, metadata, parentGui);
    }
}
