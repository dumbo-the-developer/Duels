package com.meteordevelopments.duels.gui.settings.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class KitSelectButton extends BaseButton {

    public KitSelectButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Items.from(plugin.getConfiguration().getKitSelectorButtonType(), plugin.getConfiguration().getKitSelectorButtonData()))
                .name(plugin.getLang().getMessage("GUI.settings.buttons.kit-selector.name"), plugin.getLang()).build());
    }

    @Override
    public void update(final Player player) {
        if (config.isKitSelectingUsePermission() && !player.hasPermission(Permissions.KIT_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            setLore(lang, lang.getMessage("GUI.settings.buttons.kit-selector.lore-no-permission").split("\n"));
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
        final String lore = lang.getMessage("GUI.settings.buttons.kit-selector.lore", "kit", kit);
        setLore(lang, lore.split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isKitSelectingUsePermission() && !player.hasPermission(Permissions.KIT_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.KIT_SELECTING);
            return;
        }

        com.meteordevelopments.duels.gui.customkit.CustomKitTypeSelectGui.open(plugin, player);
    }
}
