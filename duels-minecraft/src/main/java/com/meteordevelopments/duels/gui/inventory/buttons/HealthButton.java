package com.meteordevelopments.duels.gui.inventory.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HealthButton extends BaseButton {

    public HealthButton(final DuelsPlugin plugin, final Player player, final boolean dead) {
        super(plugin, ItemBuilder
                .of(dead ? Items.SKELETON_HEAD : Material.GOLDEN_APPLE)
                .name(plugin.getLang().getMessage("GUI.inventory-view.buttons.health.name", "health", dead ? 0 : Math.ceil(player.getHealth()) * 0.5), plugin.getLang())
                .build());
    }
}
