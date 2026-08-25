package com.meteordevelopments.duels.gui.settings.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.configuration.GuiItemConfig;
import com.meteordevelopments.duels.setting.Settings;
import org.bukkit.entity.Player;

public class ItemBettingButton extends BaseButton {

    private final GuiItemConfig itemConfig;
    private final boolean glow;

    public ItemBettingButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig, final boolean glow) {
        super(plugin, itemConfig.buildItem(plugin.getLang(), glow));
        this.itemConfig = itemConfig;
        this.glow = glow;
    }

    public ItemBettingButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig) {
        this(plugin, itemConfig, itemConfig.isGlowing());
    }

    @Override
    public void update(final Player player) {
        if (config.isItemBettingUsePermission() && !player.hasPermission(Permissions.ITEM_BETTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            if (itemConfig.getLoreNoPermission() != null && !itemConfig.getLoreNoPermission().isEmpty()) {
                setLore(lang, itemConfig.getLoreNoPermission().toArray(new String[0]));
            } else {
                setLore(lang, lang.getMessage("GUI.settings.buttons.item-betting.lore-no-permission").split("\n"));
            }
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String itemBetting = settings.isItemBetting() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");

        if (itemConfig.getLore() != null && !itemConfig.getLore().isEmpty()) {
            final String[] lines = itemConfig.getLore().stream()
                    .map(line -> line.replace("%item_betting%", itemBetting))
                    .toArray(String[]::new);
            setLore(lang, lines);
        } else {
            final String lore = plugin.getLang().getMessage("GUI.settings.buttons.item-betting.lore", "item_betting", itemBetting);
            setLore(lang, lore.split("\n"));
        }
    }

    @Override
    public void onClick(final Player player) {
        if (config.isItemBettingUsePermission() && !player.hasPermission(Permissions.ITEM_BETTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.ITEM_BETTING);
            return;
        }

        final Settings settings = settingManager.getSafely(player);

        if (settings.getCustomKit() != null) {
            lang.sendMessage(player, "ERROR.customkits.item-betting-disabled");
            return;
        }

        if (settings.isPartyDuel()) {
            lang.sendMessage(player, "ERROR.party-duel.option-unavailable");
            return;
        }

        settings.setItemBetting(!settings.isItemBetting());
        settings.updateGui(player);
    }
}
