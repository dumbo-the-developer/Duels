package com.meteordevelopments.duels.gui.configuration;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GuiDecoration {

    private final String key;
    private final List<Integer> slots = new ArrayList<>();
    private GuiItemConfig itemConfig;

    public GuiDecoration(final String key) {
        this.key = key;
        this.itemConfig = new GuiItemConfig();
    }

    public static GuiDecoration parse(final String key, final ConfigurationSection section) {
        if (section == null) {
            return new GuiDecoration(key);
        }

        final GuiDecoration decoration = new GuiDecoration(key);
        decoration.setItemConfig(GuiItemConfig.parse(section));
        // Use slots directly parsed into itemConfig
        decoration.getSlots().addAll(decoration.getItemConfig().getSlots());

        return decoration;
    }
}
