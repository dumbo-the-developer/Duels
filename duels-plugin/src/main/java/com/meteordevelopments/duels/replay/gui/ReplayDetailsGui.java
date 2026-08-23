package com.meteordevelopments.duels.replay.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.replay.data.DuelReplayMetadata;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.Button;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;

public class ReplayDetailsGui extends SinglePageGui<DuelsPlugin> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final DuelReplayMetadata metadata;
    private final ReplayBrowserGui parentGui;

    public ReplayDetailsGui(final DuelsPlugin plugin, final Player player, final DuelReplayMetadata metadata, final ReplayBrowserGui parentGui) {
        super(plugin, resolveTitle(plugin, metadata), 3);
        this.metadata = metadata;
        this.parentGui = parentGui;

        // Fill background with glass panes
        final Button<DuelsPlugin> filler = new Button<>(plugin, Items.GRAY_PANE.clone());
        for (int i = 0; i < 27; i++) {
            set(i, filler);
        }

        // Slot 13: Summary
        final ItemStack summaryItem = Items.HEAD.clone();
        if (metadata.getWinnerName() != null && !metadata.getWinnerName().isEmpty()) {
            final SkullMeta meta = (SkullMeta) summaryItem.getItemMeta();
            if (meta != null) {
                meta.setOwner(metadata.getWinnerName());
                summaryItem.setItemMeta(meta);
            }
        }
        set(13, new Button<DuelsPlugin>(plugin, summaryItem) {
            @Override
            public void update(final Player p) {
                final String title = plugin.getLang().getMessage("GUI.replay-details.buttons.summary.name");
                setDisplayName(title != null ? title : "&e&lMatch Details");

                final List<String> lore = new ArrayList<>();
                lore.add("&7Replay ID: &f" + metadata.getReplayId());
                lore.add("&7Player 1: &b" + (metadata.getPlayer1Name() != null ? metadata.getPlayer1Name() : "Unknown"));
                lore.add("&7Player 2: &b" + (metadata.getPlayer2Name() != null ? metadata.getPlayer2Name() : "Unknown"));
                lore.add("&7Winner: &a" + (metadata.getWinnerName() != null ? metadata.getWinnerName() : "None"));
                lore.add("&7Loser: &c" + (metadata.getLoserName() != null ? metadata.getLoserName() : "None"));
                lore.add("&7Kit: &f" + (metadata.getKitName() != null ? metadata.getKitName() : "Own Inventory"));
                lore.add("&7Arena: &f" + (metadata.getArenaName() != null ? metadata.getArenaName() : "Random"));
                lore.add("&7Duration: &e" + metadata.getFormattedDuration());
                if (metadata.getBetAmount() > 0) {
                    lore.add("&7Bet: &6$" + metadata.getBetAmount());
                }
                lore.add("&7End Reason: &7" + (metadata.getEndReason() != null ? metadata.getEndReason() : "OTHER"));
                lore.add("&7Date: &8" + DATE_FORMAT.format(new Date(metadata.getStartTime())));
                setLore(lore);
            }
        });

        // Slot 11: Play Button
        set(11, new Button<DuelsPlugin>(plugin, ItemBuilder.of(Material.EMERALD_BLOCK).name("&a&l▶ Watch Replay", plugin.getLang()).build()) {
            @Override
            public void update(final Player p) {
                final String name = plugin.getLang().getMessage("GUI.replay-details.buttons.play.name");
                setDisplayName(name != null ? name : "&a&l▶ Watch Replay");
                setLore("&7Click to enter replay viewer mode.", "&eTakes you into spectator camera!");
            }

            @Override
            public void onClick(final Player p) {
                plugin.getGuiListener().removeGui(p, ReplayDetailsGui.this);
                p.closeInventory();
                plugin.getReplayManager().playReplay(p, metadata.getReplayId());
            }
        });

        // Slot 15: Delete Button (Admin only)
        if (player.hasPermission(Permissions.ADMIN) || player.hasPermission(Permissions.REPLAY_ADMIN)) {
            set(15, new Button<DuelsPlugin>(plugin, ItemBuilder.of(Material.REDSTONE_BLOCK).name("&c&l✖ Delete Replay", plugin.getLang()).build()) {
                @Override
                public void update(final Player p) {
                    final String name = plugin.getLang().getMessage("GUI.replay-details.buttons.delete.name");
                    setDisplayName(name != null ? name : "&c&l✖ Delete Replay");
                    setLore("&7Click to permanently delete this replay.");
                }

                @Override
                public void onClick(final Player p) {
                    plugin.getReplayManager().deleteReplay(metadata.getReplayId());
                    plugin.getLang().sendMessage(p, "REPLAY.deleted", "id", metadata.getReplayId());
                    if (parentGui != null) {
                        parentGui.refresh(p);
                    } else {
                        plugin.getGuiListener().removeGui(p, ReplayDetailsGui.this);
                        p.closeInventory();
                    }
                }
            });
        }

        // Slot 22: Back Button
        set(22, new Button<DuelsPlugin>(plugin, ItemBuilder.of(Material.ARROW).name("&7« Back to Replay List", plugin.getLang()).build()) {
            @Override
            public void update(final Player p) {
                final String name = plugin.getLang().getMessage("GUI.replay-details.buttons.back.name");
                setDisplayName(name != null ? name : "&7« Back to Replay List");
            }

            @Override
            public void onClick(final Player p) {
                plugin.getGuiListener().removeGui(p, ReplayDetailsGui.this);
                if (parentGui != null) {
                    parentGui.open(p);
                } else {
                    ReplayBrowserGui.open(plugin, p, null);
                }
            }
        });
    }

    private static String resolveTitle(final DuelsPlugin plugin, final DuelReplayMetadata metadata) {
        final String msg = plugin.getLang().getMessage("GUI.replay-details.title", "id", metadata.getReplayId());
        return msg != null ? msg : "Replay: " + metadata.getReplayId();
    }

    public static void open(@NotNull final DuelsPlugin plugin, @NotNull final Player player, @NotNull final DuelReplayMetadata metadata, @Nullable final ReplayBrowserGui parentGui) {
        final ReplayDetailsGui gui = plugin.getGuiListener().addGui(player, new ReplayDetailsGui(plugin, player, metadata, parentGui), true);
        gui.open(player);
    }
}
