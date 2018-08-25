package me.realized.duels.gui.settings;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.gui.settings.buttons.ArenaSelectButton;
import me.realized.duels.gui.settings.buttons.CancelButton;
import me.realized.duels.gui.settings.buttons.ItemBettingButton;
import me.realized.duels.gui.settings.buttons.KitSelectButton;
import me.realized.duels.gui.settings.buttons.RequestDetailsButton;
import me.realized.duels.gui.settings.buttons.RequestSendButton;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.gui.SinglePageGui;
import me.realized.duels.util.inventory.Slots;
import org.bukkit.inventory.ItemStack;

public class SettingsGui extends SinglePageGui<DuelsPlugin> {

    public SettingsGui(final DuelsPlugin plugin) {
        super(plugin, plugin.getLang().getMessage("GUI.settings.title"), 3);
        final Config config = plugin.getConfiguration();
        final ItemStack spacing = Items.from(config.getSettingsFillerType(), config.getSettingsFillerData());
        Slots.run(2, 7, slot -> inventory.setItem(slot, spacing));
        Slots.run(11, 16, slot -> inventory.setItem(slot, spacing));
        Slots.run(20, 25, slot -> inventory.setItem(slot, spacing));

        set(4, new RequestDetailsButton(plugin));
        set(12, new KitSelectButton(plugin));
        set(13, new ArenaSelectButton(plugin));
        set(14, new ItemBettingButton(plugin));
        set(0, 2, 3, new RequestSendButton(plugin));
        set(7, 9, 3, new CancelButton(plugin));
    }
}
