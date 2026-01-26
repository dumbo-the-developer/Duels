package com.meteordevelopments.duels.gui.betting.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

public class HeadButton extends BaseButton {

    public HeadButton(final DuelsPlugin plugin, final Player owner) {
        super(plugin, ItemBuilder
                .of(Items.HEAD.clone())
                .name(plugin.getLang().getMessage("GUI.item-betting.buttons.head.name", "name", owner.getName()), plugin.getLang())
                .build()
        );
        setOwner(owner);
    }
}
