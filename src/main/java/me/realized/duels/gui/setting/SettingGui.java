package me.realized.duels.gui.setting;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.setting.buttons.ArenaSelectButton;
import me.realized.duels.gui.setting.buttons.CancelButton;
import me.realized.duels.gui.setting.buttons.ItemBettingButton;
import me.realized.duels.gui.setting.buttons.KitSelectButton;
import me.realized.duels.gui.setting.buttons.RequestDetailsButton;
import me.realized.duels.gui.setting.buttons.RequestSendButton;
import me.realized.duels.util.gui.SinglePageGui;
import me.realized.duels.util.inventory.InventoryUtil;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SettingGui extends SinglePageGui {

    public SettingGui(final DuelsPlugin plugin) {
        super("Request Settings", 3);

        final ItemStack spacing = ItemBuilder.of(Material.STAINED_GLASS_PANE).name(" ").build();
        InventoryUtil.fillRange(inventory, 2, 7, spacing);
        InventoryUtil.fillRange(inventory, 11, 16, spacing);
        InventoryUtil.fillRange(inventory, 20, 25, spacing);

        set(4, new RequestDetailsButton(plugin));
        set(12, new KitSelectButton(plugin));
        set(13, new ArenaSelectButton(plugin));
        set(14, new ItemBettingButton(plugin));
        set(0, 2, 3, new RequestSendButton(plugin));
        set(7, 9, 3, new CancelButton());
    }
}
