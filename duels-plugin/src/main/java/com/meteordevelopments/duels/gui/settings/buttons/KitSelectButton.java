package com.meteordevelopments.duels.gui.settings.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.configuration.GuiItemConfig;
import com.meteordevelopments.duels.gui.customkit.CustomKitTypeSelectGui;
import com.meteordevelopments.duels.setting.Settings;
import org.bukkit.entity.Player;

public class KitSelectButton extends BaseButton {

    private final GuiItemConfig itemConfig;
    private final boolean glow;

    public KitSelectButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig, final boolean glow) {
        super(plugin, itemConfig.buildItem(plugin.getLang(), glow));
        this.itemConfig = itemConfig;
        this.glow = glow;
    }

    public KitSelectButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig) {
        this(plugin, itemConfig, itemConfig.isGlowing());
    }

    @Override
    public void update(final Player player) {
        if (config.isKitSelectingUsePermission() && !player.hasPermission(Permissions.KIT_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            if (itemConfig.getLoreNoPermission() != null && !itemConfig.getLoreNoPermission().isEmpty()) {
                setLore(lang, itemConfig.getLoreNoPermission().toArray(new String[0]));
            } else {
                setLore(lang, lang.getMessage("GUI.settings.buttons.kit-selector.lore-no-permission").split("\n"));
            }
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String kit;
        if (settings.getCustomKit() != null) {
            kit = "[Custom] " + settings.getCustomKit().getName();
        } else if (settings.getKit() != null) {
            kit = settings.getKit().getName();
        } else if (settings.isOwnInventory()) {
            kit = lang.getMessage("GUI.settings.buttons.use-own-inventory.name");
        } else {
            kit = lang.getMessage("GENERAL.not-selected");
        }

        if (itemConfig.getLore() != null && !itemConfig.getLore().isEmpty()) {
            final String[] lines = itemConfig.getLore().stream()
                    .map(line -> line.replace("%kit%", kit))
                    .toArray(String[]::new);
            setLore(lang, lines);
        } else {
            final String lore = lang.getMessage("GUI.settings.buttons.kit-selector.lore", "kit", kit);
            setLore(lang, lore.split("\n"));
        }
    }

    @Override
    public void onClick(final Player player) {
        if (config.isKitSelectingUsePermission() && !player.hasPermission(Permissions.KIT_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.KIT_SELECTING);
            return;
        }

        CustomKitTypeSelectGui.open(plugin, player);
    }
}
