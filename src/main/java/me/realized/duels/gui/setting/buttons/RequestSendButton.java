package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.gui.Button;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RequestSendButton extends Button {

    public RequestSendButton(final DuelsPlugin plugin) {
        super(ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 5).name("&a&lSEND REQUEST").build());
    }

    @Override
    public void onClick(final Player player) {
        // send request
    }
}
