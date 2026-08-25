package com.meteordevelopments.duels.gui.settings.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.configuration.GuiItemConfig;
import com.meteordevelopments.duels.setting.Settings;
import org.bukkit.entity.Player;

public class OwnInventoryButton extends BaseButton {

    private final GuiItemConfig itemConfig;
    private final boolean glow;

    public OwnInventoryButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig, final boolean glow) {
        super(plugin, itemConfig.buildItem(plugin.getLang(), glow));
        this.itemConfig = itemConfig;
        this.glow = glow;
    }

    public OwnInventoryButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig) {
        this(plugin, itemConfig, itemConfig.isGlowing());
    }

    @Override
    public void update(final Player player) {
        if (config.isOwnInventoryUsePermission() && !player.hasPermission(Permissions.OWN_INVENTORY) && !player.hasPermission(Permissions.SETTING_ALL)) {
            if (itemConfig.getLoreNoPermission() != null && !itemConfig.getLoreNoPermission().isEmpty()) {
                setLore(lang, itemConfig.getLoreNoPermission().toArray(new String[0]));
            } else {
                setLore(lang, lang.getMessage("GUI.settings.buttons.use-own-inventory.lore-no-permission").split("\n"));
            }
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String ownInventory = settings.isOwnInventory() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");

        if (itemConfig.getLore() != null && !itemConfig.getLore().isEmpty()) {
            final String[] lines = itemConfig.getLore().stream()
                    .map(line -> line.replace("%own_inventory%", ownInventory))
                    .toArray(String[]::new);
            setLore(lang, lines);
        } else {
            final String lore = plugin.getLang().getMessage("GUI.settings.buttons.use-own-inventory.lore", "own_inventory", ownInventory);
            setLore(lang, lore.split("\n"));
        }
    }

    @Override
    public void onClick(final Player player) {
        if (config.isOwnInventoryUsePermission() && !player.hasPermission(Permissions.OWN_INVENTORY) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.OWN_INVENTORY);
            return;
        }

        if (!config.isKitSelectingEnabled()) {
            lang.sendMessage(player, "ERROR.duel.mode-fixed");
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        settings.setOwnInventory(!settings.isOwnInventory());
        settings.updateGui(player);
    }
}