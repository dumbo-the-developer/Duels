package com.meteordevelopments.duels.gui.settings.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.configuration.GuiItemConfig;
import org.bukkit.entity.Player;

public class CancelButton extends BaseButton {

    private final GuiItemConfig itemConfig;
    private final boolean glow;

    public CancelButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig, final boolean glow) {
        super(plugin, itemConfig.buildItem(plugin.getLang(), glow));
        this.itemConfig = itemConfig;
        this.glow = glow;
    }

    public CancelButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig) {
        this(plugin, itemConfig, itemConfig.isGlowing());
    }

    @Override
    public void onClick(final Player player) {
        player.closeInventory();
    }
}
