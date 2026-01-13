package com.meteordevelopments.duels.gui.bind;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.gui.bind.buttons.BindButton;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.MultiPageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;

import java.util.stream.Collectors;

public class BindGui extends MultiPageGui<DuelsPlugin> {

    public BindGui(final DuelsPlugin plugin, final KitImpl kit) {
        super(plugin, plugin.getLang().getMessage("GUI.bind.title", "kit", kit.getName()), plugin.getConfiguration().getArenaSelectorRows(),
                plugin.getArenaManager().getArenasImpl().stream().map(arena -> new BindButton(plugin, kit, arena)).collect(Collectors.toList()));

        final Config config = plugin.getConfiguration();
        final Lang lang = plugin.getLang();
        setSpaceFiller(Items.from(config.getArenaSelectorFillerType(), config.getArenaSelectorFillerData()));
        setPrevButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.arena-selector.buttons.previous-page.name"), lang).build());
        setNextButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.arena-selector.buttons.next-page.name"), lang).build());
        setEmptyIndicator(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.arena-selector.buttons.empty.name"), lang).build());
        // Change design to remove this loop in the future
        getButtons().forEach(button -> ((BindButton) button).setGui(this));
        calculatePages();
    }
}
