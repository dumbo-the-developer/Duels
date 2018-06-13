package me.realized.duels.gui.inventory.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HealthButton extends BaseButton {

    public HealthButton(final DuelsPlugin plugin, final Player player) {
        super(plugin, ItemBuilder
            .of(player.isDead() ? Material.SKULL_ITEM : Material.GOLDEN_APPLE)
            .name("&c" + (player.isDead() ? 0 : Math.ceil(player.getHealth()) * 0.5) + " ♥").build());
    }
}
