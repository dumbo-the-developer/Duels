package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import com.meteordevelopments.duels.util.inventory.Slots;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomKitTypeSelectGui extends SinglePageGui<DuelsPlugin> {

    public CustomKitTypeSelectGui(final DuelsPlugin plugin, final Player player) {
        super(plugin, plugin.getLang().getMessage("GUI.kit-type-selector.title"), 3);

        final ItemStack spacing = Items.from(plugin.getConfiguration().getSettingsFillerType(), plugin.getConfiguration().getSettingsFillerData());
        Slots.run(0, 26, slot -> inventory.setItem(slot, spacing));

        // Slot 11: Server Kits
        set(11, new BaseButton(plugin, ItemBuilder.of(Material.DIAMOND_SWORD)
                .name("&e&lServer Kits", plugin.getLang())
                .lore(plugin.getLang(),
                        "&7Select from predefined server duel kits.",
                        "",
                        "&aClick to browse Server Kits"
                ).build()) {
            @Override
            public void onClick(final Player player) {
                kitManager.getGui().open(player);
            }
        });

        // Slot 13: My Custom Kits
        set(13, new BaseButton(plugin, ItemBuilder.of(Material.NETHERITE_SWORD)
                .name("&b&lMy Custom Kits", plugin.getLang())
                .lore(plugin.getLang(),
                        "&7Select one of your custom duel kits.",
                        "",
                        "&aClick to browse Custom Kits"
                ).build()) {
            @Override
            public void onClick(final Player player) {
                if (!player.hasPermission(Permissions.CUSTOMKITS_USE)) {
                    plugin.getLang().sendMessage(player, "ERROR.no-permission", "permission", Permissions.CUSTOMKITS_USE);
                    return;
                }
                CustomKitSelectGui.open(plugin, player);
            }
        });

        // Slot 15: Own Inventory
        if (plugin.getConfiguration().isOwnInventoryEnabled()) {
            set(15, new BaseButton(plugin, ItemBuilder.of(Material.CHEST)
                    .name("&a&lOwn Inventory", plugin.getLang())
                    .lore(plugin.getLang(),
                            "&7Duel with items in your current inventory.",
                            "",
                            "&aClick to enable Own Inventory mode"
                    ).build()) {
                @Override
                public void onClick(final Player player) {
                    if (config.isOwnInventoryUsePermission() && !player.hasPermission(Permissions.OWN_INVENTORY) && !player.hasPermission(Permissions.SETTING_ALL)) {
                        lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.OWN_INVENTORY);
                        return;
                    }
                    final Settings settings = settingManager.getSafely(player);
                    settings.setOwnInventory(true);
                    settings.openGui(player);
                }
            });
        }

        // Slot 22: Back Button
        set(22, new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                .name("&c&lBack", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to return to Duel Settings.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                final Settings settings = settingManager.getSafely(player);
                settings.openGui(player);
            }
        });
    }

    public static void open(@NotNull final DuelsPlugin plugin, @NotNull final Player player) {
        final CustomKitTypeSelectGui gui = plugin.getGuiListener().addGui(player, new CustomKitTypeSelectGui(plugin, player), true);
        gui.open(player);
    }
}
