package com.meteordevelopments.duels.gui.options.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.options.OptionsGui;
import com.meteordevelopments.duels.gui.options.OptionsGui.Option;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OptionButton extends BaseButton {

    private final OptionsGui gui;
    private final KitImpl kit;
    private final Option option;

    public OptionButton(final DuelsPlugin plugin, final OptionsGui gui, final KitImpl kit, final Option option) {
        super(plugin, ItemBuilder.of(option.getDisplayed()).build());
        this.gui = gui;
        this.kit = kit;
        this.option = option;
        setDisplayName(plugin.getLang().getMessage("GUI.options.buttons.option.name", "name", option.name().toLowerCase()), plugin.getLang());
        update();
    }

    private void update() {
        final boolean state = option.get(kit);
        setGlow(state);

        final List<String> lore = new ArrayList<>();

        for (final String line : option.getDescription()) {
            lore.add("&f" + line.replace("%kit%", kit.getName()));
        }

        Collections.addAll(lore,
                lang.getMessage("GUI.options.buttons.option.lore", "state", state ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled")).split("\n"));
        setLore(lang, lore);
    }

    @Override
    public void onClick(final Player player) {
        option.set(kit);
        update();
        gui.update(player, this);
    }
}
