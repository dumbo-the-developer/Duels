package com.meteordevelopments.duels.replay.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.replay.data.DuelReplayMetadata;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.MultiPageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;

public class ReplayBrowserGui extends MultiPageGui<DuelsPlugin> {

    private final UUID targetPlayer;

    public ReplayBrowserGui(final DuelsPlugin plugin, final UUID targetPlayer) {
        super(plugin,
                resolveTitle(plugin, targetPlayer),
                4,
                new ArrayList<>());
        this.targetPlayer = targetPlayer;

        setSpaceFiller(Items.ORANGE_PANE);

        final String prevName = plugin.getLang().getMessage("GUI.replay-browser.buttons.previous-page.name");
        setPrevButton(ItemBuilder.of(Material.PAPER)
                .name(prevName != null ? prevName : "&a« Previous Page", plugin.getLang())
                .build());

        final String nextName = plugin.getLang().getMessage("GUI.replay-browser.buttons.next-page.name");
        setNextButton(ItemBuilder.of(Material.PAPER)
                .name(nextName != null ? nextName : "&aNext Page »", plugin.getLang())
                .build());

        final String emptyName = plugin.getLang().getMessage("GUI.replay-browser.buttons.empty.name");
        setEmptyIndicator(ItemBuilder.of(Material.PAPER)
                .name(emptyName != null ? emptyName : "&cNo replays found.", plugin.getLang())
                .build());

        populateButtons();
        calculatePages();
    }

    private static String resolveTitle(final DuelsPlugin plugin, final UUID targetPlayer) {
        if (targetPlayer != null) {
            final OfflinePlayer offline = Bukkit.getOfflinePlayer(targetPlayer);
            final String name = offline.getName() != null ? offline.getName() : "Unknown";
            final String msg = plugin.getLang().getMessage("GUI.replay-browser.player-title", "player", name);
            return msg != null ? msg : "Replays: " + name;
        }
        final String msg = plugin.getLang().getMessage("GUI.replay-browser.title");
        return msg != null ? msg : "Duel Replays";
    }

    private void populateButtons() {
        getButtons().clear();
        final List<DuelReplayMetadata> list;
        if (targetPlayer != null) {
            list = plugin.getReplayManager().getMetadataForPlayer(targetPlayer);
        } else {
            list = plugin.getReplayManager().getAllMetadata();
        }

        for (final DuelReplayMetadata meta : list) {
            ((List<ReplayEntryButton>) getButtons()).add(new ReplayEntryButton(plugin, meta, this));
        }
    }

    public void refresh(final Player player) {
        populateButtons();
        calculatePages();
        if (player != null && player.isOnline()) {
            open(player);
        }
    }

    public static void open(@NotNull final DuelsPlugin plugin, @NotNull final Player player, @Nullable final UUID targetPlayer) {
        final ReplayBrowserGui gui = plugin.getGuiListener().addGui(player, new ReplayBrowserGui(plugin, targetPlayer), true);
        gui.open(player);
    }
}
