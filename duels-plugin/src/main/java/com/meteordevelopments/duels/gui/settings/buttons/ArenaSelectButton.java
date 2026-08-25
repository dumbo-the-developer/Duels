package com.meteordevelopments.duels.gui.settings.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.configuration.GuiItemConfig;
import com.meteordevelopments.duels.setting.Settings;
import org.bukkit.entity.Player;

public class ArenaSelectButton extends BaseButton {

    private final GuiItemConfig itemConfig;
    private final boolean glow;

    public ArenaSelectButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig, final boolean glow) {
        super(plugin, itemConfig.buildItem(plugin.getLang(), glow));
        this.itemConfig = itemConfig;
        this.glow = glow;
    }

    public ArenaSelectButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig) {
        this(plugin, itemConfig, itemConfig.isGlowing());
    }

    @Override
    public void update(final Player player) {
        if (config.isArenaSelectingUsePermission() && !player.hasPermission(Permissions.ARENA_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            if (itemConfig.getLoreNoPermission() != null && !itemConfig.getLoreNoPermission().isEmpty()) {
                setLore(lang, itemConfig.getLoreNoPermission().toArray(new String[0]));
            } else {
                setLore(lang, lang.getMessage("GUI.settings.buttons.arena-selector.lore-no-permission").split("\n"));
            }
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String arena = settings.getArena() != null ? settings.getArena().getName() : lang.getMessage("GENERAL.random");

        if (itemConfig.getLore() != null && !itemConfig.getLore().isEmpty()) {
            final String[] lines = itemConfig.getLore().stream()
                    .map(line -> line.replace("%arena%", arena))
                    .toArray(String[]::new);
            setLore(lang, lines);
        } else {
            final String lore = lang.getMessage("GUI.settings.buttons.arena-selector.lore", "arena", arena);
            setLore(lang, lore.split("\n"));
        }
    }

    @Override
    public void onClick(final Player player) {
        if (config.isArenaSelectingUsePermission() && !player.hasPermission(Permissions.ARENA_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.ARENA_SELECTING);
            return;
        }

        arenaManager.getGui().open(player);
    }
}
