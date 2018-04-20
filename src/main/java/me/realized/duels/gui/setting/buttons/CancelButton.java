package me.realized.duels.gui.setting.buttons;

import me.realized.duels.util.gui.Button;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CancelButton extends Button {

    public CancelButton() {
        super(ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 14).name("&c&lCANCEL").build());
    }

    @Override
    public void onClick(final Player player) {
        player.closeInventory();
    }
}
