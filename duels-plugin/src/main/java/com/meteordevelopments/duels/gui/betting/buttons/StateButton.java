package com.meteordevelopments.duels.gui.betting.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.betting.BettingGui;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StateButton extends BaseButton {

    private final BettingGui gui;
    private final UUID owner;

    public StateButton(final DuelsPlugin plugin, final BettingGui gui, final Player owner) {
        super(plugin, ItemBuilder
                .of(Items.OFF.clone())
                .name(plugin.getLang().getMessage("GUI.item-betting.buttons.state.name-not-ready"), plugin.getLang())
                .build()
        );
        this.gui = gui;
        this.owner = owner.getUniqueId();
    }

    @Override
    public void onClick(final Player player) {
        if (!gui.isReady(player) && player.getUniqueId().equals(owner)) {
            setDisplayed(Items.ON.clone());
            setDisplayName(lang.getMessage("GUI.item-betting.buttons.state.name-ready"), lang);
            gui.update(player, this);
            gui.setReady(player);
        }
    }
}
