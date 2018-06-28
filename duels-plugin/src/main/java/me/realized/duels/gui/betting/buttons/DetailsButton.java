package me.realized.duels.gui.betting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class DetailsButton extends BaseButton {

    private final Settings settings;

    public DetailsButton(final DuelsPlugin plugin, final Settings settings) {
        super(plugin, ItemBuilder
            .of(Material.SIGN)
            .name(plugin.getLang().getMessage("GUI.item-betting.buttons.details.name"))
            .build()
        );
        this.settings = settings;
    }

    @Override
    public void update(final Player player) {
        final String lore = lang.getMessage("GUI.item-betting.buttons.details.lore",
            "kit", settings.getKit() != null ? settings.getKit().getName() : "Not Selected",
            "arena", settings.getArena() != null ? settings.getArena().getName() : "Random",
            "bet_amount", settings.getBet()
        );
        setLore(lore.split("\n"));
    }
}
