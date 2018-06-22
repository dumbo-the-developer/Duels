package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CancelButton extends BaseButton {

    public CancelButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 14).name(plugin.getLang().getMessage("GUI.settings.buttons.cancel.name")).build());
    }

    @Override
    public void onClick(final Player player) {
        player.closeInventory();
    }
}
